/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.projectforge.registry.Registry;
import org.projectforge.web.UserFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class LoginDefaultHandler implements LoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoginDefaultHandler.class);

  private UserDao userDao;

  /**
   * Only needed if the data-base needs an update first (may-be the PFUserDO can't be read because of unmatching tables).
   */
  private DataSource dataSource;

  /**
   * @see org.projectforge.user.LoginHandler#initialize(org.projectforge.registry.Registry)
   */
  @Override
  public void initialize()
  {
    final Registry registry = Registry.instance();
    userDao = (UserDao) registry.getDao(UserDao.class);
    dataSource = registry.getDataSource();
  }

  /**
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @SuppressWarnings({ "unchecked", "rawtypes"})
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    final LoginResult loginResult = new LoginResult();
    final String encryptedPassword = userDao.encryptPassword(password);
    PFUserDO user = null;
    if (UserFilter.isUpdateRequiredFirst() == true) {
      // Only administrator login is allowed. The login is checked without Hibernate because the data-base schema may be out-dated thus
      // Hibernate isn't functioning.
      final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
      try {
        final PFUserDO resUser = new PFUserDO();
        final String sql = "select pk, firstname, lastname from t_pf_user where username=? and password=? and deleted=false";
        jdbc.query(sql, new Object[] { username, encryptedPassword}, new ResultSetExtractor() {
          @Override
          public Object extractData(final ResultSet rs) throws SQLException, DataAccessException
          {
            if (rs.next() == true) {
              final int pk = rs.getInt("pk");
              final String firstname = rs.getString("firstname");
              final String lastname = rs.getString("lastname");
              resUser.setId(pk);
              resUser.setUsername(username).setFirstname(firstname).setLastname(lastname);
            }
            return null;
          }
        });
        if (resUser.getUsername() == null) {
          log.info("Admin login for maintenance (data-base update) failed for user '" + username + "' (user/password not found).");
          return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
        }
        if (isAdminUser(resUser) == false) {
          return loginResult.setLoginResultStatus(LoginResultStatus.ADMIN_LOGIN_REQUIRED);
        }
        return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(resUser);
      } catch (final Exception ex) {
        log.error(ex.getMessage(), ex);
      }
    } else {
      user = userDao.authenticateUser(username, encryptedPassword);
    }
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return loginResult.setLoginResultStatus(LoginResultStatus.LOGIN_EXPIRED);
      } else {
        return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
  }

  public boolean isAdminUser(final PFUserDO user)
  {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    String sql = "select pk from t_group where name=?";
    final int adminGroupId = jdbc.queryForInt(sql, new Object[] { ProjectForgeGroup.ADMIN_GROUP.getKey()});
    sql = "select count(*) from t_group_user where group_id=? and user_id=?";
    final int count = jdbc.queryForInt(sql, new Object[] { adminGroupId, user.getId()});
    if (count != 1) {
      log.info("Admin login for maintenance (data-base update) failed for user '"
          + user.getUsername()
          + "' (user not member of admin group).");
      return false;
    }
    return true;
  }

  /**
   * @see org.projectforge.user.LoginHandler#checkStayLogin(org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean checkStayLogin(final PFUserDO user)
  {
    final PFUserDO dbUser = userDao.getUserGroupCache().getUser(user.getId());
    if (dbUser != null && dbUser.isDeleted() == false) {
      return true;
    }
    log.warn("User is deleted, stay-logged-in denied for the given user: " + user);
    return false;
  }
}
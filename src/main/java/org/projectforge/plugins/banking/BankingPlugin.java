/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.banking;

import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * Your plugin initialization. Register all your components such as i18n files, data-access object etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class BankingPlugin extends AbstractPlugin
{
  public static final String BANK_ACCOUNT_ID = "bankAccount";

  public static final String BANK_ACCOUNT_BALANCE_ID = "bankAccountBalance";

  public static final String BANK_ACCOUNT_RECORD_ID = "bankAccountRecord";

  public static final String RESOURCE_BUNDLE_NAME = BankingPlugin.class.getPackage().getName() + ".BankingI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { BankAccountDO.class, BankAccountBalanceDO.class,
    BankAccountRecordDO.class};

  private BankAccountDao bankAccountDao;

  // private BankAccountBalanceDao addressCampaignValueDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    BankingPluginUpdates.dao = getDatabaseUpdateDao();
    // Register it:
    register(BANK_ACCOUNT_ID, BankAccountDao.class, bankAccountDao, "plugins.banking.account").setNestedDOClasses(
        BankAccountRecordDO.class, BankAccountBalanceDO.class).setSearchable(false);
    // register(BANK_ACCOUNT_BALANCE_ID, BankAccountBalanceDao.class, addressCampaignValueDao, "plugins.banking.accountBalance");
    registerRight(new BankAccountRight());
    // Register the web part:
    registerWeb(BANK_ACCOUNT_ID, BankAccountListPage.class, BankAccountEditPage.class);
    // registerWeb(BANK_ACCOUNT_BALANCE_ID, BankAccountBalanceListPage.class, BankAccountBalanceEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.FIBU);
    registerMenuItem(new MenuItemDef(parentMenu, BANK_ACCOUNT_ID, 100, "plugins.banking.account.menu", BankAccountListPage.class,
        BankAccountDao.USER_RIGHT_ID));
    // registerMenuItem(new MenuItemDef(parentMenu, BANK_ACCOUNT_BALANCE_ID, 30, "plugins.banking.bankAccountBalance.menu",
    // BankAccountBalanceListPage.class));

    // Define the access management:
    // registerRight(new BankAccountRight());
    // registerRight(new BankAccountBalanceRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setBankAccountDao(final BankAccountDao bankAccountDao)
  {
    this.bankAccountDao = bankAccountDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return BankingPluginUpdates.getInitializationUpdateEntry();
  }
}

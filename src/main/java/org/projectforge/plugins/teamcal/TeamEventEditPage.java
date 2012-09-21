/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventEditPage extends AbstractEditPage<TeamEventDO, TeamEventEditForm, TeamEventDao>
{
  private static final long serialVersionUID = 1221484611148024273L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventEditPage.class);

  /**
   * Key for preset the start date.
   */
  public static final String PARAMETER_KEY_START_DATE_IN_MILLIS = "startMillis";

  /**
   * Key for preset the stop date.
   */
  public static final String PARAMETER_KEY_END_DATE_IN_MILLIS = "endMillis";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_START_DATE = "newStartDate";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_END_DATE = "newEndDate";

  /**
   * Key for calendar.
   */
  public static final String PARAMETER_KEY_TEAMCALID = "teamCalId";

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamEventEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamevent");
    super.init();
  }

  @SuppressWarnings("null")
  void preInit()
  {
    if (isNew() == true) {
      final PageParameters parameters = getPageParameters();
      final Long startDateInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_START_DATE_IN_MILLIS);
      final Long stopTimeInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_END_DATE_IN_MILLIS);
      final String teamCalId = WicketUtils.getAsString(parameters, PARAMETER_KEY_TEAMCALID);
      if (startDateInMillis != null) {
        getData().setStartDate(new Timestamp(startDateInMillis));
        if (stopTimeInMillis == null) {
          getData().setEndDate(new Timestamp(stopTimeInMillis)); // Default is time sheet with zero duration.
        }
      }
      if (stopTimeInMillis != null) {
        getData().setEndDate(new Timestamp(stopTimeInMillis));
        if (startDateInMillis == null) {
          getData().setStartDate(new Timestamp(startDateInMillis)); // Default is time sheet with zero duration.
        }
      }
      if (teamCalId != null) {
        getData().setCalendar(teamCalDao.getById(Integer.valueOf(teamCalId))); // TODO trycatch
      }
    } else {
      final Long newStartTimeInMillis = WicketUtils.getAsLong(getPageParameters(), PARAMETER_KEY_START_DATE_IN_MILLIS);
      final Long newStopTimeInMillis = WicketUtils.getAsLong(getPageParameters(), PARAMETER_KEY_END_DATE_IN_MILLIS);
      if (newStartTimeInMillis != null) {
        getData().setStartDate(new Timestamp(newStartTimeInMillis));
      }
      if (newStopTimeInMillis != null) {
        getData().setEndDate(new Timestamp(newStopTimeInMillis));
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    super.create();
    final PageParameters params = new PageParameters();
    params.add("id", getData().getCalendar().getId());
    final TeamCalEditPage page = new TeamCalEditPage(params);
    setResponsePage(page);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#cancel()
   */
  @Override
  protected void cancel()
  {
    final PageParameters params = new PageParameters();
    params.add("id", getData().getCalendar().getId());
    final TeamCalEditPage page = new TeamCalEditPage(params);
    setResponsePage(page);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamEventDao getBaseDao()
  {
    return teamEventDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage, org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamEventEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamEventDO data)
  {
    return new TeamEventEditForm(this, data);
  }

}
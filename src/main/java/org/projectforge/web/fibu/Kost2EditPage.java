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

package org.projectforge.web.fibu;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = Kost2ListPage.class)
public class Kost2EditPage extends AbstractEditPage<Kost2DO, Kost2EditForm, Kost2Dao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 1622932678249306556L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost2EditPage.class);

  @SpringBean(name = "kost2Dao")
  private Kost2Dao kost2Dao;

  public Kost2EditPage(final PageParameters parameters)
  {
    super(parameters, "fibu.kost2");
    init();
  }

  @Override
  protected Kost2Dao getBaseDao()
  {
    return kost2Dao;
  }

  @Override
  protected Kost2EditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final Kost2DO data)
  {
    return new Kost2EditForm(this, data);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("projektId".equals(property) == true) {
      kost2Dao.setProjekt(getData(), (Integer) selectedValue);
      form.nummernkreisField.modelChanged();
      form.bereichField.modelChanged();
      form.teilbereichField.modelChanged();
      form.kost2ArtField.modelChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(final String property)
  {
    if ("projektId".equals(property) == true) {
      getData().setProjekt(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}

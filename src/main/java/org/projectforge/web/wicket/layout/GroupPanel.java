/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GroupPanel extends Panel
{
  private static final long serialVersionUID = -8760386387270114082L;

  /**
   * The markup wicket id of the heading label.
   */
  public static final String HEADING_ID = "heading";

  private Label headingLabel;

  private RepeatingView entriesRepeater;

  public GroupPanel(final String id)
  {
    super(id);
  }

  public GroupPanel(final String id, final String heading)
  {
    this(id);
    setHeading(heading);
  }

  public GroupPanel add(final AbstractLayoutPanel layoutPanel)
  {
    entriesRepeater.add(layoutPanel);
    return this;
  }

  public String newChildId()
  {
    if (entriesRepeater == null) {
      init();
    }
    return entriesRepeater.newChildId();
  }

  /**
   * Should only be called manually if no children are added to this field set. Otherwise it'll be initialized at the first call of
   * newChildId().
   */
  public GroupPanel init()
  {
    if (entriesRepeater != null) {
      return this;
    }
    if (this.headingLabel != null) {
      add(this.headingLabel);
    } else {
      add(new Label(HEADING_ID, "[invisible]").setVisible(false));
    }
    entriesRepeater = new RepeatingView("entriesRepeater");
    add(entriesRepeater);
    return this;
  }

  public void setHeading(final Label headingLabel)
  {
    this.headingLabel = headingLabel;
  }

  public void setHeading(final String heading)
  {
    this.headingLabel = new Label(HEADING_ID, heading);
  }
}

/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.wicket.flowlayout;

/**
 * Used for defining class attribute value for elements.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum DivType
{
  ACCORDION_BOX("accordion_box"), BLOCK("block"), BLOCK_LINES("block lines"), BOX("box"), CLEARFIX("clearfix mm_clear"), COL_25("span3"), COL_33(
      "span4"), COL_40("span4"), COL_50("span6"), COL_60("span8"), COL_66("span8"), COL_75("span9"), COL_100("span12"), COLUMNS(
          "columns"), CHECKBOX("jqui_checkbox"), FIELD_DIV("field_div"), GRID4("span4"), GRID6("span6"), GRID12("span12"), MARGIN_TOP_10(
              "margin_top_10"), RADIOBOX("radio-jquery-ui"), ROUND_ALL("round_all"), ROW_FLUID("row-fluid"), SECTION("section"), TOGGLE_CONTAINER(
                  "toggle_container");

  private String classAttrValue;

  public String getClassAttrValue()
  {
    return classAttrValue;
  }

  private DivType(final String classAttrValue)
  {
    this.classAttrValue = classAttrValue;
  }
}

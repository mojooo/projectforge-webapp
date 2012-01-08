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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = EmployeeEditPage.class)
public class EmployeeListPage extends AbstractListPage<EmployeeListForm, EmployeeDao, EmployeeDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "employeeDao")
  private EmployeeDao employeeDao;

  public EmployeeListPage(PageParameters parameters)
  {
    super(parameters, "fibu.employee");
  }

  public EmployeeListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.employee");
  }

  @SuppressWarnings("serial")
  public List<IColumn<EmployeeDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    List<IColumn<EmployeeDO>> columns = new ArrayList<IColumn<EmployeeDO>>();

    CellItemListener<EmployeeDO> cellItemListener = new CellItemListener<EmployeeDO>() {
      public void populateItem(Item<ICellPopulator<EmployeeDO>> item, String componentId, IModel<EmployeeDO> rowModel)
      {
        final EmployeeDO employee = rowModel.getObject();
        if (employee.getStatus() == null) {
          // Should not occur:
          return;
        }
        String cellStyle = "";
        if (employee.isDeleted() == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("name")),
        getSortable("user.lastname", sortable), "user.lastname", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final EmployeeDO employee = (EmployeeDO) rowModel.getObject();
        final PFUserDO user = employee != null ? employee.getUser() : null;
        final String lastname = user != null ? user.getLastname() : null;
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, EmployeeEditPage.class, employee.getId(), returnToPage, lastname));
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, employee.getId(), lastname));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("firstName")), getSortable("user.firstname",
        sortable), "user.firstname", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("status")), getSortable("status", sortable),
        "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("fibu.kost1")), getSortable(
        "kost1.shortDisplayName", sortable), "kost1.shortDisplayName", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("address.positionText")), getSortable(
        "position", sortable), "position", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("address.division")), getSortable("abteilung",
        sortable), "abteilung", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("fibu.employee.eintrittsdatum")), getSortable(
        "eintrittsDatum", sortable), "eintrittsDatum", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<EmployeeDO>> item, final String componentId, final IModel<EmployeeDO> rowModel)
      {
        final EmployeeDO employee = (EmployeeDO) rowModel.getObject();
        item.add(new Label(componentId, DateTimeFormatter.instance().getFormattedDate(employee.getEintrittsDatum())));
      }
    });
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("fibu.employee.austrittsdatum")), getSortable(
        "austrittsDatum", sortable), "austrittsDatum", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<EmployeeDO>> item, final String componentId, final IModel<EmployeeDO> rowModel)
      {
        final EmployeeDO employee = (EmployeeDO) rowModel.getObject();
        item.add(new Label(componentId, DateTimeFormatter.instance().getFormattedDate(employee.getAustrittsDatum())));
      }
    });
    columns.add(new CellItemListenerPropertyColumn<EmployeeDO>(new Model<String>(getString("comment")), getSortable("comment", sortable),
        "comment", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    final List<IColumn<EmployeeDO>> columns = createColumns(this, true);
    dataTable = createDataTable(columns, "user.lastname", true);
    form.add(dataTable);
  }

  @Override
  protected EmployeeListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new EmployeeListForm(this);
  }

  @Override
  protected EmployeeDao getBaseDao()
  {
    return employeeDao;
  }

  @Override
  protected IModel<EmployeeDO> getModel(EmployeeDO object)
  {
    return new DetachableDOModel<EmployeeDO, EmployeeDao>(object, getBaseDao());
  }

  protected EmployeeDao getEmployeeDao()
  {
    return employeeDao;
  }
}

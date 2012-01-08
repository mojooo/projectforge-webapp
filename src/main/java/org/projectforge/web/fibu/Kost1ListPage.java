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
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.export.ContentProvider;
import org.projectforge.export.ExportColumn;
import org.projectforge.export.ExportSheet;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.I18nExportColumn;
import org.projectforge.export.PropertyMapping;
import org.projectforge.export.XlsContentProvider;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = Kost1EditPage.class)
public class Kost1ListPage extends AbstractListPage<Kost1ListForm, Kost1Dao, Kost1DO> implements IListPageColumnsCreator<Kost1DO>
{
  private static final long serialVersionUID = 2432908214495492575L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost1ListPage.class);

  @SpringBean(name = "kost1Dao")
  private Kost1Dao kost1Dao;

  public Kost1ListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.kost1");
  }

  public Kost1ListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.kost1");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<Kost1DO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<Kost1DO>> columns = new ArrayList<IColumn<Kost1DO>>();
    CellItemListener<Kost1DO> cellItemListener = new CellItemListener<Kost1DO>() {
      public void populateItem(Item<ICellPopulator<Kost1DO>> item, String componentId, IModel<Kost1DO> rowModel)
      {
        final Kost1DO kost1 = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(kost1.getId(), kost1.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<Kost1DO>(new Model<String>(getString("fibu.kost1")), getSortable("formattedNumber", sortable),
        "formattedNumber", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final Kost1DO kost1 = (Kost1DO) rowModel.getObject();
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, Kost1EditPage.class, kost1.getId(), returnToPage, String.valueOf(kost1
              .getFormattedNumber())));
          cellItemListener.populateItem(item, componentId, rowModel);
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, kost1.getId(), String.valueOf(kost1
              .getFormattedNumber())));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost1DO>(new Model<String>(getString("description")), getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost1DO>(new Model<String>(getString("status")), getSortable("kostentraegerStatus", sortable),
        "kostentraegerStatus", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "formattedNumber", true);
    form.add(dataTable);
  }

  private enum Col
  {
    STATUS, KOST, DESCRIPTION;
  }

  void exportExcel()
  {
    log.info("Exporting kost1 list.");
    refresh();
    final List<Kost1DO> kost1List = getList();
    if (kost1List == null || kost1List.size() == 0) {
      // Nothing to export.
      form.addError("validation.error.nothingToExport");
      return;
    }
    final String filename = "ProjectForge-Kost1Export_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xls";
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new XlsContentProvider(xls);
    xls.setContentProvider(contentProvider);
    final ExportSheet sheet = xls.addSheet(PFUserContext.getLocalizedString("fibu.kost1.kost1s"));
    final ExportColumn[] cols = new ExportColumn[] { //
    new I18nExportColumn(Col.KOST, "fibu.kost1", XlsContentProvider.LENGTH_KOSTENTRAEGER),
        new I18nExportColumn(Col.DESCRIPTION, "description", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.STATUS, "status", XlsContentProvider.LENGTH_STD)};
    sheet.setColumns(cols);
    final PropertyMapping mapping = new PropertyMapping();
    for (final Kost1DO kost : kost1List) {
      mapping.add(Col.KOST, kost.getFormattedNumber());
      mapping.add(Col.STATUS, kost.getKostentraegerStatus());
      mapping.add(Col.DESCRIPTION, kost.getDescription());
      sheet.addRow(mapping.getMapping(), 0);
    }
    sheet.setZoom(3, 4); // 75%
    DownloadUtils.setDownloadTarget(xls.getAsByteArray(), filename);
  }

  @Override
  protected Kost1ListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new Kost1ListForm(this);
  }

  @Override
  protected Kost1Dao getBaseDao()
  {
    return kost1Dao;
  }

  @Override
  protected IModel<Kost1DO> getModel(Kost1DO object)
  {
    return new DetachableDOModel<Kost1DO, Kost1Dao>(object, getBaseDao());
  }

  protected Kost1Dao getKost1Dao()
  {
    return kost1Dao;
  }
}

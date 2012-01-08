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

package org.projectforge.plugins.marketing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.common.StringHelper;
import org.projectforge.web.address.AddressEditPage;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.components.CheckBoxPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = AddressCampaignValueEditPage.class)
public class AddressCampaignValueListPage extends AbstractListPage<AddressCampaignValueListForm, AddressDao, AddressDO> implements
IListPageColumnsCreator<AddressDO>
{
  private static final long serialVersionUID = -2418497742599443358L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "addressCampaignValueDao")
  private AddressCampaignValueDao addressCampaignValueDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  Map<Integer, PersonalAddressDO> personalAddressMap;

  Map<Integer, AddressCampaignValueDO> addressCampaignValueMap;

  public AddressCampaignValueListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.marketing.addressCampaignValue");
    newItemMenuEntry.setVisibilityAllowed(false);
  }

  @Override
  protected void onBodyTag(final ComponentTag bodyTag)
  {
    bodyTag.put("onload", "javascript:setOptionStatus();");
  }

  public List<IColumn<AddressDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    return createColumns(returnToPage, sortable, false);
  }

  public List<IColumn<AddressDO>> createColumns(final WebPage returnToPage, final boolean sortable, final boolean massUpdateMode)
  {
    return createColumns(returnToPage, sortable, massUpdateMode, form.getSearchFilter(), personalAddressMap, addressCampaignValueMap);
  }

  @SuppressWarnings("serial")
  protected static final List<IColumn<AddressDO>> createColumns(final WebPage page, final boolean sortable, final boolean massUpdateMode,
      final AddressCampaignValueFilter searchFilter, final Map<Integer, PersonalAddressDO> personalAddressMap,
      final Map<Integer, AddressCampaignValueDO> addressCampaignValueMap)
      {

    final List<IColumn<AddressDO>> columns = new ArrayList<IColumn<AddressDO>>();
    final CellItemListener<AddressDO> cellItemListener = new CellItemListener<AddressDO>() {
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final Serializable highlightedRowId;
        if (page instanceof AbstractListPage< ? , ? , ? >) {
          highlightedRowId = ((AbstractListPage< ? , ? , ? >) page).getHighlightedRowId();
        } else {
          highlightedRowId = null;
        }
        final PersonalAddressDO personalAddress = personalAddressMap.get(address.getId());
        final StringBuffer cssStyle = getCssStyle(address.getId(), highlightedRowId, address.isDeleted());
        if (address.isDeleted() == true) {
          // Do nothing further
        } else if (personalAddress != null && personalAddress.isFavoriteCard() == true) {
          cssStyle.append("color: red;");
        }
        if (address.getAddressStatus().isIn(AddressStatus.LEAVED, AddressStatus.OUTDATED) == true
            || address.getContactStatus().isIn(ContactStatus.DEPARTED, ContactStatus.NON_ACTIVE, ContactStatus.PERSONA_INGRATA,
                ContactStatus.UNINTERESTING, ContactStatus.DEPARTED) == true) {
          cssStyle.append("text-decoration: line-through;");
        }
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    if (page instanceof AddressCampaignValueMassUpdatePage) {
      columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("created")), getSortable("created",
          sortable), "created", cellItemListener));
    } else if (massUpdateMode == true) {
      columns.add(new CellItemListenerPropertyColumn<AddressDO>("", null, "selected", cellItemListener) {
        @Override
        public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
        {
          final AddressDO address = rowModel.getObject();
          final CheckBoxPanel checkBoxPanel = new CheckBoxPanel(componentId, new PropertyModel<Boolean>(address, "selected"), true);
          item.add(checkBoxPanel);
          cellItemListener.populateItem(item, componentId, rowModel);
          addRowClick(item, massUpdateMode);
        }
      });
    } else {
      columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("created")), getSortable("created",
          sortable), "created", cellItemListener) {
        @SuppressWarnings("unchecked")
        @Override
        public void populateItem(final Item item, final String componentId, final IModel rowModel)
        {
          final AddressDO address = (AddressDO) rowModel.getObject();
          final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(address.getId());
          final Integer addressCampaignValueId = addressCampaignValue != null ? addressCampaignValue.getId() : null;
          item.add(new ListSelectActionPanel(componentId, rowModel, AddressCampaignValueEditPage.class, addressCampaignValueId, page,
              DateTimeFormatter.instance().getFormattedDateTime(address.getCreated()), AddressCampaignValueEditPage.PARAMETER_ADDRESS_ID,
              String.valueOf(address.getId()), AddressCampaignValueEditPage.PARAMETER_ADDRESS_CAMPAIGN_ID, String.valueOf(searchFilter
                  .getAddressCampaignId())));
          addRowClick(item);
          cellItemListener.populateItem(item, componentId, rowModel);
        }
      });
    }
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("name")), getSortable("name", sortable),
        "name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("firstName")), getSortable("firstName",
        sortable), "firstName", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("organization")), getSortable(
        "organization", sortable), "organization", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("address.contactStatus")), getSortable(
        "contactStatus", sortable), "contactStatus", cellItemListener));
    columns.add(new AbstractColumn<AddressDO>(new Model<String>(page.getString("address.addressText"))) {
      @Override
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final String addressText = StringHelper.listToString("|", address.getMailingAddressText(), address.getMailingZipCode()
            + " "
            + address.getMailingCity(), address.getMailingCountry());
        if (massUpdateMode == false) {
          final Fragment fragment = new Fragment(componentId, "addressEditLink", page);
          item.add(fragment);
          fragment.add(new Link<Object>("link") {
            @Override
            public void onClick()
            {
              final PageParameters parameters = new PageParameters();
              parameters.put(AbstractEditPage.PARAMETER_KEY_ID, address.getId());
              final AddressEditPage editPage = new AddressEditPage(parameters);
              editPage.setReturnToPage(page);
              setResponsePage(editPage);
            }
          }.add(new Label("label", addressText).setRenderBodyOnly(true)));
        } else {
          item.add(new Label(componentId, addressText));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(page.getString("address.addressStatus")), getSortable(
        "addressStatus", sortable), "addressStatus", cellItemListener));
    columns.add(new AbstractColumn<AddressDO>(new Model<String>(page.getString("value"))) {
      @Override
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final Integer id = address.getId();
        final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(id);
        if (addressCampaignValue != null) {
          item.add(new Label(componentId, addressCampaignValue.getValue()));
          item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
        } else {
          item.add(new Label(componentId, ""));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new AbstractColumn<AddressDO>(new Model<String>(page.getString("comment"))) {
      @Override
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final Integer id = address.getId();
        final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(id);
        if (addressCampaignValue != null) {
          item.add(new Label(componentId, addressCampaignValue.getComment()));
          item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
        } else {
          item.add(new Label(componentId, ""));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    return columns;
      }

  @Override
  protected void onNextSubmit()
  {
    final ArrayList<AddressDO> list = new ArrayList<AddressDO>();
    for (final AddressDO sheet : getList()) {
      if (sheet.isSelected() == true) {
        list.add(sheet);
      }
    }
    setResponsePage(new AddressCampaignValueMassUpdatePage(this, list, form.getSearchFilter().getAddressCampaign(), personalAddressMap,
        addressCampaignValueMap));
  }

  @Override
  public boolean isSupportsMassUpdate()
  {
    return true;
  }

  @Override
  protected void onBeforeRender()
  {
    addressCampaignValueDao.getAddressCampaignValuesByAddressId(addressCampaignValueMap, form.getSearchFilter());
    super.onBeforeRender();
  }

  @Override
  protected void init()
  {
    personalAddressMap = personalAddressDao.getPersonalAddressByAddressId();
    addressCampaignValueMap = new HashMap<Integer, AddressCampaignValueDO>();
  }

  @Override
  public List<AddressDO> getList()
  {
    if (list == null) {
      super.getList();
      final String value = form.getSearchFilter().getAddressCampaignValue();
      if (StringUtils.isEmpty(value) == false) {
        final List<AddressDO> origList = list;
        list = new ArrayList<AddressDO>();
        for (final AddressDO address : origList) {
          final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(address.getId());
          if (addressCampaignValue != null && addressCampaignValue.getValue() != null) {
            if (value.equals(addressCampaignValue.getValue()) == true) {
              list.add(address);
            }
          } else {
            // address campaign value of the given address is not set:
            if (AddressCampaignValueListForm.ADDRESS_CAMPAIGN_VALUE_UNDEFINED.equals(value) == true) {
              // Filter all address campaign values without defined value:
              list.add(address);
            }
          }
        }
      }
    }
    return list;
  }

  @Override
  protected void createDataTable()
  {
    final List<IColumn<AddressDO>> columns = createColumns(this, !isMassUpdateMode(), isMassUpdateMode());
    dataTable = createDataTable(columns, "name", true);
    form.add(dataTable);
  }

  @Override
  public void refresh()
  {
    super.refresh();
    if (form.getSearchFilter().isNewest() == true && StringUtils.isBlank(form.getSearchFilter().getSearchString()) == true) {
      form.getSearchFilter().setMaxRows(form.getPageSize());
    }
  }

  @Override
  protected AddressCampaignValueListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AddressCampaignValueListForm(this);
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected IModel<AddressDO> getModel(final AddressDO object)
  {
    return new DetachableDOModel<AddressDO, AddressDao>(object, getBaseDao());
  }

  protected AddressCampaignValueDao getAddressCampaignValueDao()
  {
    return addressCampaignValueDao;
  }
}

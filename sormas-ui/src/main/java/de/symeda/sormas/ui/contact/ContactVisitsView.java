/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.contact;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.contact.ContactStatus;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.visit.VisitCriteria;
import de.symeda.sormas.api.visit.VisitDto;
import de.symeda.sormas.api.visit.VisitExportDto;
import de.symeda.sormas.api.visit.VisitExportType;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateFormatHelper;
import de.symeda.sormas.ui.utils.DownloadUtil;
import de.symeda.sormas.ui.visit.VisitGrid;

import java.util.Date;

public class ContactVisitsView extends AbstractContactView {

	private static final long serialVersionUID = -1L;

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/visits";

	private VisitCriteria criteria;
	
	private VisitGrid grid;    
	private Button newButton;
	private VerticalLayout gridLayout;
//	private HashMap<Button, String> statusButtons;
//	private Button activeStatusButton;

	public ContactVisitsView() {
		super(VIEW_NAME);

		setSizeFull();
		
		criteria = ViewModelProviders.of(ContactVisitsView.class).get(VisitCriteria.class);
	}

	public HorizontalLayout createTopBar() {
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setSpacing(true);
		topLayout.setWidth(100, Unit.PERCENTAGE);

//		statusButtons = new HashMap<>();
//
//		Button contactButton = new Button(I18nProperties.getCaption(Captions.contactRelated), e -> {
//			grid.reload(getContactRef());
//			processStatusChangeVisuals(e.getButton());
//		});
//		CssStyles.style(contactButton, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER);
//		contactButton.setCaptionAsHtml(true);
//		topLayout.addComponent(contactButton);
//		statusButtons.put(contactButton, I18nProperties.getCaption(Captions.contactRelated));
//
//		Button personButton = new Button(I18nProperties.getCaption(Captions.contactPersonVisits), e -> {
//			ContactDto contact = FacadeProvider.getContactFacade().getContactByUuid(getContactRef().getUuid());
//			grid.reload(contact.getPerson());
//			processStatusChangeVisuals(e.getButton());
//		});
//		CssStyles.style(personButton, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER, CssStyles.BUTTON_FILTER_LIGHT);
//		personButton.setCaptionAsHtml(true);
//		topLayout.addComponent(personButton);
//		statusButtons.put(personButton, I18nProperties.getCaption(Captions.contactPersonVisits));

//		topLayout.setExpandRatio(topLayout.getComponent(topLayout.getComponentCount()-1), 1);

		if (UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			topLayout.setWidth(100, Unit.PERCENTAGE);

			MenuBar bulkOperationsDropdown = new MenuBar();	
			MenuItem bulkOperationsItem = bulkOperationsDropdown.addItem(I18nProperties.getCaption(Captions.bulkActions), null);

			Command deleteCommand = selectedItem -> {
				ControllerProvider.getVisitController().deleteAllSelectedItems(grid.asMultiSelect().getSelectedItems(), new Runnable() {
					public void run() {
						navigateTo(criteria);
					}
				});
			};
			bulkOperationsItem.addItem(I18nProperties.getCaption(Captions.bulkDelete), VaadinIcons.TRASH, deleteCommand);

			topLayout.addComponent(bulkOperationsDropdown);
			topLayout.setComponentAlignment(bulkOperationsDropdown, Alignment.TOP_RIGHT);
			topLayout.setExpandRatio(bulkOperationsDropdown, 1);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.VISIT_EXPORT)) {
			Button exportButton = new Button(I18nProperties.getCaption(Captions.export));
			{
				exportButton.setId("export");
				exportButton.setIcon(VaadinIcons.DOWNLOAD);
				exportButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				topLayout.addComponent(exportButton);
				topLayout.setComponentAlignment(exportButton, Alignment.TOP_RIGHT);
			}

			StreamResource exportStreamResource = DownloadUtil.createCsvExportStreamResource(VisitExportDto.class, VisitExportType.CONTACT_VISITS,
					(Integer start, Integer max) -> FacadeProvider.getVisitFacade().getVisitsExportList(grid.getCriteria(), VisitExportType.CONTACT_VISITS, start, max, null),
					(propertyId, type) -> {
						String caption = findPrefixCaption(propertyId,
								VisitExportDto.I18N_PREFIX,
								VisitDto.I18N_PREFIX,
								PersonDto.I18N_PREFIX,
								SymptomsDto.I18N_PREFIX);
						if (Date.class.isAssignableFrom(type)) {
							caption += " (" + DateFormatHelper.getDateFormatPattern() + ")";
						}
						return caption;
					},
					createFileNameWithCurrentDate("sormas_contact_visits_", ".csv"), null);

			new FileDownloader(exportStreamResource).extend(exportButton);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.VISIT_CREATE)) {
			newButton = new Button(I18nProperties.getCaption(Captions.visitNewVisit));
			newButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			newButton.setIcon(VaadinIcons.PLUS_CIRCLE);
			newButton.addClickListener(e -> {
				ControllerProvider.getVisitController().createVisit(this.getContactRef(), 
						r -> navigateTo(criteria));
			});
			topLayout.addComponent(newButton);
			topLayout.setComponentAlignment(newButton, Alignment.MIDDLE_RIGHT);
		}

		topLayout.addStyleName(CssStyles.VSPACE_3);
//		activeStatusButton = contactButton;
		return topLayout;
	}

//	private void updateActiveStatusButtonCaption() {
//		if (activeStatusButton != null) {
//			activeStatusButton.setCaption(statusButtons.get(activeStatusButton) + LayoutUtil.spanCss(CssStyles.BADGE, String.valueOf(grid.getContainer().size())));
//		}
//	}

//	private void processStatusChangeVisuals(Button button) {
//		statusButtons.keySet().forEach(b -> {
//			CssStyles.style(b, CssStyles.BUTTON_FILTER_LIGHT);
//			b.setCaption(statusButtons.get(b));
//		});
//		CssStyles.removeStyles(button, CssStyles.BUTTON_FILTER_LIGHT);
//		activeStatusButton = button;
//		updateActiveStatusButtonCaption();
//	}

	@Override
	public void enter(ViewChangeEvent event) {
		super.enter(event);
		
		// Hide the "New visit" button for converted contacts
		if (newButton != null
				&& FacadeProvider.getContactFacade().getContactByUuid(getContactRef().getUuid()).getContactStatus() == ContactStatus.CONVERTED) {
			newButton.setVisible(false);
		}

		criteria.contact(getContactRef());
		
		if (grid == null) {
			grid = new VisitGrid(criteria);
			gridLayout = new VerticalLayout();
			gridLayout.setSizeFull();
			gridLayout.setMargin(true);
			gridLayout.setSpacing(false);
			gridLayout.addComponent(createTopBar());
			gridLayout.addComponent(grid);
			gridLayout.setExpandRatio(grid, 1);
			setSubComponent(gridLayout);
		}
		
		grid.reload();
//		updateActiveStatusButtonCaption();
	}

}

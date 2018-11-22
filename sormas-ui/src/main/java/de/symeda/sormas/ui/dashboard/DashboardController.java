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
package de.symeda.sormas.ui.dashboard;

import com.vaadin.navigator.Navigator;

import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.dashboard.contacts.DashboardContactsView;
import de.symeda.sormas.ui.dashboard.surveillance.DashboardSurveillanceView;
import de.symeda.sormas.ui.login.LoginHelper;

public class DashboardController {

	public DashboardController() {
		
	}

	public void registerViews(Navigator navigator) {
		if (LoginHelper.hasUserRight(UserRight.DASHBOARD_SURVEILLANCE_ACCESS)) {
			navigator.addView(DashboardSurveillanceView.VIEW_NAME, DashboardSurveillanceView.class);
		}
		if (LoginHelper.hasUserRight(UserRight.DASHBOARD_CONTACT_ACCESS)) {
			navigator.addView(DashboardContactsView.VIEW_NAME, DashboardContactsView.class);
		}
	}
	
}
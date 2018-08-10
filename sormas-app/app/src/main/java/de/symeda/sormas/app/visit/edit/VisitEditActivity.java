package de.symeda.sormas.app.visit.edit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

import java.util.List;

import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.api.visit.VisitStatus;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.visit.Visit;
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.symptoms.SymptomsEditFragment;
import de.symeda.sormas.app.util.Bundler;
import de.symeda.sormas.app.visit.VisitSection;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;

public class VisitEditActivity extends BaseEditActivity<Visit> {

    public static final String TAG = VisitEditActivity.class.getSimpleName();

    private AsyncTask saveTask;

    private String contactUuid = null;

    public static void startActivity(Context context, String rootUuid, String contactUuid, VisitSection section) {
        BaseEditActivity.startActivity(context, VisitEditActivity.class, buildBundle(rootUuid, contactUuid, section));
    }

    public static Bundler buildBundle(String rootUuid, String contactUuid, VisitSection section) {
        return buildBundle(rootUuid, section).setContactUuid(contactUuid);
    }

    @Override
    protected void onCreateInner(Bundle savedInstanceState) {
        super.onCreateInner(savedInstanceState);
        contactUuid = new Bundler(savedInstanceState).getContactUuid();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new Bundler(outState).setContactUuid(contactUuid);
    }

    @Override
    protected Visit queryRootEntity(String recordUuid) {
        return DatabaseHelper.getVisitDao().queryUuid(recordUuid);
    }

    @Override
    protected Visit buildRootEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VisitStatus getPageStatus() {
        return getStoredRootEntity() == null ? null : getStoredRootEntity().getVisitStatus();
    }

    @Override
    public List<PageMenuItem> getPageMenuData() {
        return PageMenuItem.fromEnum(VisitSection.values(), getContext());
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, Visit activityRootData) {

        VisitSection section = VisitSection.fromOrdinal(menuItem.getKey());
        BaseEditFragment fragment;
        switch (section) {
            case VISIT_INFO:
                fragment = VisitEditFragment.newInstance(activityRootData, contactUuid);
                break;
            case SYMPTOMS:
                fragment = SymptomsEditFragment.newInstance(activityRootData);
                break;
            default:
                throw new IllegalArgumentException(DataHelper.toStringNullable(section));
        }

        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getSaveMenu().setTitle(R.string.action_save_followup);
        return result;
    }

    @Override
    public void saveData() {
        final Visit visit = getStoredRootEntity();

        try {
            FragmentValidator.validate(getContext(), getActiveFragment().getContentBinding());
        } catch (ValidationException e) {
            NotificationHelper.showNotification(this, ERROR, e.getMessage());
            return;
        }

        saveTask = new SavingAsyncTask(getRootView(), visit) {

            @Override
            protected void onPreExecute() {
                showPreloader();
            }

            @Override
            public void doInBackground(TaskResultHolder resultHolder) throws Exception {
                DatabaseHelper.getVisitDao().saveAndSnapshot(visit);
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                hidePreloader();
                super.onPostExecute(taskResult);
                if (taskResult.getResultStatus().isSuccess()) {
                    goToNextPage();
                } else {
                    onResume(); // reload data
                }
            }
        }.executeOnThreadPool();
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_level3_1_contact_visit_info;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }
}
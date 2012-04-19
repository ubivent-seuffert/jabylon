package de.jutzig.jabylon.ui.config.internal;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.prefs.Preferences;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.jutzig.jabylon.cdo.connector.RepositoryConnector;
import de.jutzig.jabylon.cdo.server.ServerConstants;
import de.jutzig.jabylon.properties.Workspace;
import de.jutzig.jabylon.ui.applications.MainDashboard;
import de.jutzig.jabylon.ui.config.AbstractConfigSection;
import de.jutzig.jabylon.ui.config.ConfigSection;
import de.jutzig.jabylon.users.User;
import de.jutzig.jabylon.users.UsersFactory;

public class UserConfig extends AbstractConfigSection<Workspace> implements ConfigSection {
	RepositoryConnector connector = MainDashboard.getCurrent().getRepositoryConnector();
	CDOView view;
	Table userTable = null;
	Object selectedItem = null;

	@SuppressWarnings("serial")
	@Override
	public Component createContents() {
		VerticalLayout userConfig = new VerticalLayout();
		userTable = new Table("Users", getUsers());
		userTable.setSizeFull();
		userTable.setSelectable(true);
		userTable.addListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				selectedItem = event.getItemId();
			}
		});
		userConfig.addComponent(userTable);
		HorizontalLayout buttonLine = new HorizontalLayout();
		Button addUser = new Button("Add User");
		addUser.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addUser();
			}
		});
		Button deleteUser = new Button("Delete User");
		deleteUser.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				deleteUser();
			}
		});
		buttonLine.addComponent(addUser);
		buttonLine.addComponent(deleteUser);
		userConfig.addComponent(buttonLine);
		return userConfig;
	}

	private void addUser() {
		final Window addUser = new Window("Add user");
		addUser.setModal(true);
		addUser.setHeight("180px");
		addUser.setWidth("280px");
		final Form addUserForm = new Form();
		User user = UsersFactory.eINSTANCE.createUser();
		addUserForm.setItemDataSource(new BeanItem<User>(user));
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(addUserForm);
		Button ok = new Button("Submit");
		ok.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				CDOTransaction transaction = connector.openTransaction();
				CDOResource resource = transaction.getOrCreateResource(ServerConstants.USERS_RESOURCE);
				resource.getContents().add(((BeanItem<User>)addUserForm.getItemDataSource()).getBean());
				try {
					transaction.commit();
				} catch (CommitException e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					transaction.close();
					MainDashboard.getCurrent().getMainWindow().removeWindow(addUser);
				}
			}
		});
		layout.addComponent(ok);
		layout.setMargin(true);
		addUser.setContent(layout);
		MainDashboard.getCurrent().getMainWindow().addWindow(addUser);
}

	private void deleteUser() {

	}

	private Container getUsers() {
		view = connector.openView();
		EList<EObject> users = null;
		if(view.hasResource(ServerConstants.USERS_RESOURCE)) {
			CDOResource resource = view.getResource(ServerConstants.USERS_RESOURCE);
			users = resource.getContents();
		} else {
			CDOTransaction transaction = connector.openTransaction();
			CDOResource resource = transaction.createResource(ServerConstants.USERS_RESOURCE);
			User user = UsersFactory.eINSTANCE.createUser();
			user.setName("admin");
			resource.getContents().add(user);
			try {
				transaction.commit();
				users = view.getResource(ServerConstants.USERS_RESOURCE).getContents();
			} catch (final CommitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				transaction.close();
			}
		}

		BeanItemContainer<User> usersContainer = new BeanItemContainer<User>(User.class);
		for(EObject obj : users) {
			if(obj instanceof User) {
				usersContainer.addBean((User)obj);
			}
		}
		return usersContainer;
	}

	@Override
	public void commit(Preferences config) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void init(Preferences config) {

	}

}

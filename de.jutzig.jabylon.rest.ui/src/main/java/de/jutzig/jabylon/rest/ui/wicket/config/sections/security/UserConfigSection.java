package de.jutzig.jabylon.rest.ui.wicket.config.sections.security;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.osgi.service.prefs.Preferences;

import de.jutzig.jabylon.rest.ui.model.EObjectPropertyModel;
import de.jutzig.jabylon.rest.ui.wicket.components.UserImagePanel;
import de.jutzig.jabylon.rest.ui.wicket.config.AbstractConfigSection;
import de.jutzig.jabylon.security.CommonPermissions;
import de.jutzig.jabylon.users.User;
import de.jutzig.jabylon.users.UsersPackage;

public class UserConfigSection extends GenericPanel<User> {

	private static final long serialVersionUID = 1L;

	public UserConfigSection(String id, IModel<User> model) {
		super(id, model);
		
		boolean isLDAP = CommonPermissions.AUTH_TYPE_LDAP.equals(model.getObject().getType());
		
		add(new UserImagePanel("image", getModel(),true));
		RequiredTextField<String> userID = new RequiredTextField<String>("username",new EObjectPropertyModel<String,User>(getModel(), UsersPackage.Literals.USER__NAME));
		userID.setEnabled(!isLDAP);
		add(userID);
		PasswordTextField passwordTextField = new PasswordTextField("userpassword",new EObjectPropertyModel<String,User>(getModel(), UsersPackage.Literals.USER__PASSWORD));
		passwordTextField.setRequired(!isLDAP);
		passwordTextField.setEnabled(!isLDAP);
		passwordTextField.setResetPassword(false);
		add(passwordTextField);
		
		TextField<String> emailField = new TextField<String>("email",new EObjectPropertyModel<String,User>(getModel(), UsersPackage.Literals.USER__EMAIL));
		emailField.setEnabled(!isLDAP);
		add(emailField);
		emailField.add(EmailAddressValidator.getInstance());
		TextField<String> displayName = new TextField<String>("displayName",new EObjectPropertyModel<String,User>(getModel(), UsersPackage.Literals.USER__DISPLAY_NAME));
		displayName.setEnabled(!isLDAP);
		add(displayName);

		TextField<String> type = new TextField<String>("type",new EObjectPropertyModel<String,User>(getModel(), UsersPackage.Literals.USER__TYPE));
		type.setEnabled(false);
		add(type);
	}

	public static class UserConfigSectionContributor extends AbstractConfigSection<User> {

		private static final long serialVersionUID = 1L;

		@Override
		public WebMarkupContainer doCreateContents(String id, IModel<User> input, Preferences config) {

			return new UserConfigSection(id, input);
		}

		@Override
		public void commit(IModel<User> input, Preferences config) {


		}


		@Override
		public String getRequiredPermission() {
			return CommonPermissions.USER_GLOBAL_CONFIG;
		}
	}
}

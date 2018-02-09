# Admininistrator manual


This manual is meant for users with admin permissions – for information on tasks regarding the  administration of NanoDefiner e-tool installations (i.e. installation and configuration) refer to the README text files.

The features only available to administrator users can be split into *user administration* and *developer* features. User administration features include:

- activating or deactivating users
- granting and removing administration permissions
- creating and changing user accounts

Developer features include:

- basic issue management
- locale coverage

### User administration

The user administration features are available from the **Manage users** menu item in the user menu.

The first tab (**"Activate users"**) shows you a list of users which are currently not activated, a very useful feature if manual activation of new users is activated. To activate users, simply click on the checkboxes in the corresponding row and click the **Activate** button above the table.

The second tab (**"User management"**) shows a list of all users and allows you to activate or deactivate (green checkmark / red cross in the **Activated** column) and grant or remove admin permissions (green checkmark / red cross in the **Admin** column). Click on the pencil in the **Actions** column will open a form similar to the user registration form where you can change most aspects of the selected user account, including creating a new password for the user.

To create a new account (which will automatically be activated after creation), choose **New user** in the action box above the tab list.

### Developer features

*Issues* can be managed from the corresponding user menu item, which shows lists of issues separated into active, archived, and all, much like the lists of other entities within the application. Issues consist of a reporting user, a creation and change date, as well as an admin comment. When clicking on an issue, you can review all of these information and change the comment.

Issues can be created in two ways, first by using the red **Feedback** button at the lower right corner of the page (TODO document option to disable feedback button), and the second is by using the **Report problem** button on an error page. In the latter case, detailed error information are automatically included in the issue text as well.

In the application configuration, you can enable the automatic creation of issues when an error occurred.

**Locale coverage** is a feature most useful during development to find unused locale strings. It is disabled by default and there should be no reason to enable it in production environments.

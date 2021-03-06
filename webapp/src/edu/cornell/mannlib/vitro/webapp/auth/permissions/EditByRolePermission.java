/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.permissions;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * Is the user authorized to edit properties that are marked as restricted to a
 * certain "Role Level"?
 */
public class EditByRolePermission extends Permission {
	private static final Log log = LogFactory
			.getLog(EditByRolePermission.class);

	public static final String NAMESPACE = "java:"
			+ EditByRolePermission.class.getName() + "#";

	private final String roleName;
	private final RoleLevel roleLevel;
	private final ServletContext ctx;

	public EditByRolePermission(String roleName, RoleLevel roleLevel,
			ServletContext ctx) {
		super(NAMESPACE + roleName);

		if (roleName == null) {
			throw new NullPointerException("role may not be null.");
		}
		if (roleLevel == null) {
			throw new NullPointerException("roleLevel may not be null.");
		}
		if (ctx == null) {
			throw new NullPointerException("context may not be null.");
		}

		this.roleName = roleName;
		this.roleLevel = roleLevel;
		this.ctx = ctx;
	}

	/**
	 * If the requested action is to edit a property statement, we might
	 * authorize it based on their role level.
	 */
	@Override
	public boolean isAuthorized(RequestedAction whatToAuth) {
		boolean result;

		if (whatToAuth instanceof AbstractDataPropertyStatementAction) {
			result = isAuthorized((AbstractDataPropertyStatementAction) whatToAuth);
		} else if (whatToAuth instanceof AbstractObjectPropertyStatementAction) {
			result = isAuthorized((AbstractObjectPropertyStatementAction) whatToAuth);
		} else {
			result = false;
		}

		if (result) {
			log.debug(this + " authorizes " + whatToAuth);
		} else {
			log.debug(this + " does not authorize " + whatToAuth);
		}

		return result;
	}

	/**
	 * The user may add, edit, or delete this data property if they are allowed
	 * to modify its subject and its predicate.
	 */
	private boolean isAuthorized(AbstractDataPropertyStatementAction action) {
		String subjectUri = action.getSubjectUri();
		String predicateUri = action.getPredicateUri();
		return canModifyResource(subjectUri)
				&& canModifyPredicate(predicateUri);
	}

	/**
	 * The user may add, edit, or delete this data property if they are allowed
	 * to modify its subject, its predicate, and its object.
	 */
	private boolean isAuthorized(AbstractObjectPropertyStatementAction action) {
		String subjectUri = action.getSubjectUri();
		String predicateUri = action.getPredicateUri();
		String objectUri = action.getObjectUri();
		return canModifyResource(subjectUri)
				&& canModifyPredicate(predicateUri)
				&& canModifyResource(objectUri);
	}

	private boolean canModifyResource(String resourceUri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyResource(
				resourceUri, roleLevel);
	}

	private boolean canModifyPredicate(String predicateUri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyPredicate(
				predicateUri, roleLevel);
	}

	@Override
	public String toString() {
		return "EditByRolePermission['" + roleName + "']";
	}

}

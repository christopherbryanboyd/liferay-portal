package ${packagePath}.model.impl;

import ${serviceBuilder.getCompatJavaClassName("StringBundler")};

import ${apiPackagePath}.model.${entity.name};

<#if entity.hasLocalService() && entity.hasEntityColumns()>
	import ${apiPackagePath}.service.${entity.name}LocalServiceUtil;

	import com.liferay.portal.kernel.exception.PortalException;
	import com.liferay.portal.kernel.exception.SystemException;
	import com.liferay.portal.kernel.model.TreeModel;

	import java.util.ArrayList;
	import java.util.List;
</#if>

/**
 * The extended model base implementation for the ${entity.name} service. Represents a row in the &quot;${entity.table}&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This class exists only as a container for the default extended model level methods generated by ServiceBuilder. Helper methods and all application logic should be put in {@link ${entity.name}Impl}.
 * </p>
 *
 * @author ${author}
 * @see ${entity.name}Impl
 * @see ${apiPackagePath}.model.${entity.name}
<#if classDeprecated>
 * @deprecated ${classDeprecatedComment}
</#if>
 * @generated
 */

<#if classDeprecated>
	@Deprecated
</#if>
public abstract class ${entity.name}BaseImpl extends ${entity.name}ModelImpl implements ${entity.name} {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. All methods that expect a ${entity.humanName} model instance should use the <code>${entity.name}</code> interface instead.
	 */

	<#if entity.hasLocalService() && entity.hasEntityColumns() && entity.hasPersistence()>
		@Override
		public void persist() {
			if (this.isNew()) {
				${entity.name}LocalServiceUtil.add${entity.name}(this);
			}
			else {
				<#if entity.versionEntity??>
					try {
						${entity.name}LocalServiceUtil.update${entity.name}(this);
					}
					catch (PortalException portalException) {
						throw new SystemException(portalException);
					}
				<#else>
					${entity.name}LocalServiceUtil.update${entity.name}(this);
				</#if>
			}
		}

		<#if entity.isTreeModel()>
			<#assign pkEntityColumn = entity.PKEntityColumns?first />

			<#if entity.hasEntityColumn("parent" + pkEntityColumn.methodName)>
				@Override
				@SuppressWarnings("unused")
				public String buildTreePath() throws PortalException {
					List<${entity.name}> ${entity.varNames} = new ArrayList<${entity.name}>();

					${entity.name} ${entity.varName} = this;

					while (${entity.varName} != null) {
						${entity.varNames}.add(${entity.varName});

						${entity.varName} = ${entity.name}LocalServiceUtil.fetch${entity.name}(${entity.varName}.getParent${pkEntityColumn.methodName}());
					}

					StringBundler sb = new StringBundler(${entity.varNames}.size() * 2 + 1);

					sb.append("/");

					for (int i = ${entity.varNames}.size() - 1; i >= 0; i--) {
						${entity.varName} = ${entity.varNames}.get(i);

						sb.append(${entity.varName}.get${entity.PKEntityColumns[0].methodName}());
						sb.append("/");
					}

					return sb.toString();
				}
			</#if>

			@Override
			public void updateTreePath(String treePath) {
				${entity.name} ${entity.varName} = this;

				${entity.varName}.setTreePath(treePath);

				<#if entity.versionEntity??>
					try {
						${entity.name}LocalServiceUtil.update${entity.name}(${entity.varName});
					}
					catch (PortalException portalException) {
						throw new SystemException(portalException);
					}
				<#else>
					${entity.name}LocalServiceUtil.update${entity.name}(${entity.varName});
				</#if>
			}
		</#if>
	</#if>

}
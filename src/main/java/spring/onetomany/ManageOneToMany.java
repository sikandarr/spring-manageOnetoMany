package spring.onetomany;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
public class ManageOneToMany
{
	@Autowired
	private WebApplicationContext appContext;
	private static Repositories repositories;
	static Logger log = Logger.getLogger(ManageOneToMany.class.getName());

	@PostConstruct
	public void init()
	{
		repositories = new Repositories(appContext);
	}

	public static <T, K> void addChildren(T parent, List<K> children)
	{
		if (children == null || children.size() == 0)
		{
			log.warn("no elements in children list or is null");
			throw new IllegalArgumentException("no elements in children list or is null");
		}

		Class<?> childClass = children.get(0).getClass();
		@SuppressWarnings("unchecked")
		CrudRepository<K, ?> childRepo = (CrudRepository<K, ?>) repositories.getRepositoryFor(childClass);

		Method setParentMethodInChild = null;
		Method getParentMethodInChild = null;
		for (Method method : childClass.getMethods())
		{
			if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals(parent.getClass()))
				setParentMethodInChild = method;

			if (method.getReturnType().equals(parent.getClass()))
				getParentMethodInChild = method;
		}

		for (K child : children)
		{
			try
			{
				if (getParentMethodInChild.invoke(child) == null)
				{
					setParentMethodInChild.invoke(child, parent);
					childRepo.save(child);
				}
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				log.error(
						"exception in ManagingOneToMany.addChildren(" + parent.getClass().getSimpleName() + ", "
								+ children.getClass().getSimpleName() + "<" + children.get(0).getClass() + ">" + ")",
						e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, K> void syncChildren(T instance, List<K> children)
	{
		if (children == null || children.size() == 0)
		{
			log.warn("no elements in children list or is null");
			throw new IllegalArgumentException("no elements in children list or is null");
		}

		Class<?> childClass = children.get(0).getClass();
		Class<?> parentClass = instance.getClass();
		CrudRepository<K, ?> childRepo = (CrudRepository<K, ?>) repositories.getRepositoryFor(childClass);

		try
		{
			Field parentIdField = null;
			for (Field field : parentClass.getDeclaredFields())
				if (field.isAnnotationPresent(Id.class))
					parentIdField = field;

			Field childIdField = null;
			Field parentFieldInChildClass = null;
			for (Field field : childClass.getDeclaredFields())
			{
				if (field.isAnnotationPresent(Id.class))
					childIdField = field;

				if (field.isAnnotationPresent(ManyToOne.class))
					if (field.getType().equals(parentClass))
						parentFieldInChildClass = field;
			}

			Method findByParentIdMethodInChildRepo = childRepo.getClass().getMethod(
					"findBy" + capitalize(parentFieldInChildClass.getName()) + capitalize(parentIdField.getName()),
					parentIdField.getType());

			Method getIdMethodInParent = parentClass.getMethod("get" + capitalize(parentIdField.getName()));
			Method getIdMethodInChild = childClass.getMethod("get" + capitalize(childIdField.getName()));
			Object parentIdValue = getIdMethodInParent.invoke(instance);

			List<K> oldChildren = (List<K>) findByParentIdMethodInChildRepo.invoke(childRepo, parentIdValue);

			if (oldChildren.size() > children.size())
				for (K oldChild : oldChildren)
				{
					boolean found = false;
					for (K child : children)
					{
						if (getIdMethodInChild.invoke(oldChild).equals(getIdMethodInChild.invoke(child)))
							found = true;
					}
					if (!found)
						childRepo.delete(oldChild);
				}
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			log.error("exception in ManagingOneToMany.syncChildren(" + instance.getClass().getSimpleName() + ", "
					+ children.getClass().getSimpleName() + "<" + children.get(0).getClass() + ">" + ")", e);
		}
	}

	private static String capitalize(String word)
	{
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
}

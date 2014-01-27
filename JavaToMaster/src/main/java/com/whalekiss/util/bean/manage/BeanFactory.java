package com.whalekiss.util.bean.manage;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.whalekiss.util.concurrent.PlatformReentrantReadWriteLock;
import com.whalekiss.util.concurrent.PlatformReentrantReadWriteLock.PlatformReadLock;
import com.whalekiss.util.concurrent.PlatformReentrantReadWriteLock.PlatformWriteLock;

/**
 * A class that provides static methods for getting instances
 * of all managed beans. The actual internal factory used
 * is configurable and may be overriden with the system 
 * property -Dcom.expedia.platform.beans.BeanFactory.class.
 * User beans should be declared in one or more 
 * business_services.xml Spring configuration files placed
 * anywhere in the classpath.
 *
 * Some comments added by cstein.
 *
 * @author <a href="mailto:brentk@expedia.com">Brent Krum</a>
 * @author <a href="mailto:adoerhoefer@expedia.com">Alan Doerhoefer</a>
 *
 */

abstract public class BeanFactory
{
	private static IBeanFactory s_beanFactoryToUse = null;
	private static Logger s_logger = Logger.getLogger(BeanFactory.class);
	private static PlatformReentrantReadWriteLock s_initLock = new PlatformReentrantReadWriteLock();
	private static ManualResetEvent s_beanFactoryOpenedEvent = new ManualResetEvent();

	/**
	 * Opens the bean factory in order to get instances of managed beans.
	 */
	public static void open()
	{
		logToDebug("BeanFactory.open()");

		PlatformWriteLock lock = s_initLock.writeLock();

		lock.lock();
		try
		{
			if(s_beanFactoryToUse == null)		// NOPMD Is thread safe in a way PMD does not understand.
			{
				try
				{
					s_beanFactoryToUse = BeanFactorySingleton.getFactory();
					BeanFactorySingleton.open();
				}
				catch(Exception ex)
				{
					try
					{
						s_logger.error("BeanFactory.open failed due to an exception, closing the beanfactory", ex);
						BeanFactorySingleton.close();
					}
					finally
					{
						s_beanFactoryToUse = null;
					}
					s_beanFactoryOpenedEvent.setSignal();
					throw new BeanFactoryNotOpenedException(ex);
				}
				s_beanFactoryOpenedEvent.setSignal();
			}
			else
			{
				s_beanFactoryOpenedEvent.setSignal();
				throw new BeanFactoryAlreadyOpenException();
			}
		}
		finally
		{
			lock.unlock();
		}

		logToDebug("BeanFactory.open() returns");
	}

	/**
	 * This method waits a specified amount of time for the bean
	 * factory to open. 
	 * 
	 * @param timeoutInMs The time (in milliseconds) to wait.
	 * @return Returns true if the bean factory opened before the 
	 * timeout elapsed, otherwise false.
	 */
	public static boolean waitForOpen(final long timeoutInMs)
	{
		logToDebug("BeanFactory.waitForOpen(" + timeoutInMs + ")");

		boolean	result;
		try
		{
			if (!s_beanFactoryOpenedEvent.lookForSignal(timeoutInMs))
			{
				result = false;
			}
			else
			{
				result = s_beanFactoryToUse != null;
			}
		}
		catch(InterruptedException ex)
		{
			result = false;
		}

		logToDebug("BeanFactory.waitForOpen() returns " + result);
		return result;
	}

	/**
	 * Closes the bean factory.
	 */
	public static void close()
	{
		logToDebug("BeanFactory.close()");

		PlatformWriteLock lock = s_initLock.writeLock();

		lock.lock();
		try
		{
			if(s_beanFactoryToUse != null)		// NOPMD Is thread safe in a way PMD does not understand.
			{
				try
				{
					BeanFactorySingleton.close();
				}
				finally
				{
					s_beanFactoryToUse = null;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets the bean instance for a given bean name.
	 * @param beanName The fully-qualified bean name.
	 * @return Returns the bean instance.
	 */
	public static Object getBean(String beanName)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getBean(beanName);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets the bean instance for a given bean name
	 * that must match the type given.
	 * @param name The fully-qualified bean name.
	 * @param expectedClassType The specific class type that is expected.
	 * @return Returns the bean instance.
	 */
   @SuppressWarnings("unchecked")		// NOPMD
	public static <T> T getBean(String name, Class<T> expectedClassType)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return (T)s_beanFactoryToUse.getBean(name, expectedClassType);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets all beans of the given type.
	 * @param type The type of bean to get.
	 * @return Map of all beans of the given type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> getBeansOfType(Class<T> type)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getBeansOfType(type);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Returns true if the bean for the given bean name exists
	 * in the list of managed beans.
	 * @param beanName The fully-qualified bean name.
	 * @return Returns true if bean exists; false otherwise.
	 */
	public static boolean containsBean(String beanName)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.containsBean(beanName);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets a list of bean definition names per context.
	 * 
	 * @return A Map<String, String[]> of bean definition names where 
	 * the key is the context and value is the list of bean definition
	 * names.
	 */
	public static Map<String, String[]> getBeanDefinitionNamesByContext()
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getBeanDefinitionNamesByContext();
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets an array of bean names for the specified type.
	 * 
	 * @param type The type to find bean names for.
	 * @return Returns the bean names for the specified type as
	 * an array of Strings. 
	 */
	public static String[] getBeanNamesForType(Class type)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getBeanNamesForType(type);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets an array of bean names for the specified type.
	 * 
	 * @param type The type to find bean names for.
	 * @param includePrototypes True to include prototypes in the list.
	 * @param includeFactoryBeans True to include factory beans in the list.
	 * @return Returns the bean names for the specified type as
	 * an array of Strings.
	 */
	public static String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getBeanNamesForType(type, includePrototypes, includeFactoryBeans);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets the aliases for the specified bean name.
	 * 
	 * @param name The fully-qualified bean name.
	 * @return Returns a list of alias as a String array.
	 */
	public static String[] getAliases(String name)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getAliases(name);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets a list of bean aliases for the specified context.
	 * 
	 * @param name The name of the context to find aliases for.
	 * @return Returns a Map<String, String[]> of bean aliases for the
	 * specified context where the key is the context and the value is 
	 * the list of bean aliases.
	 */
	public static Map<String, String[]> getAliasesByContext(String name)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getAliasesByContext(name);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Gets the class type for the specified fully-qualified bean name.
	 * 
	 * @param name The fully-qualified bean name.
	 * @return Returns the class type for the specified bean name.
	 */
	public static Class getType(String name)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.getType(name);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Determines whether the specified bean is a singleton. 
	 * 
	 * @param name The fully-qualified name of the bean.
	 * @return Returns true if the specified bean is a singleton, 
	 * otherwise false.
	 */
	public static boolean isSingleton(String name)
	{
		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				return s_beanFactoryToUse.isSingleton(name);
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Refreshes the bean factory by reloading the the beans in the
	 * refreshable context and any beans that have registered themselves
	 * as being refreshable.
	 * 
	 * @return Returns true if the refresh was successful.
	 * @throws BeanFactoryRefreshException
	 */
	public static boolean refresh() throws BeanFactoryRefreshException
	{
		return refresh(null);
	}
	
	/**
	 * Refreshes the bean factory by reloading the the beans in the
	 * refreshable context and any beans that have registered themselves
	 * as being refreshable. This refreshes the particular resource set type only.
	 * 
	 * @param resourceSetTypes the resource set types to refresh.
	 * @return Returns true if the refresh was successful.
	 * @throws BeanFactoryRefreshException
	 * 
	 * @since v1.22
	 */
	public static boolean refresh(List<IActivityResourceSetType> resourceSetTypes) throws BeanFactoryRefreshException
	{
		logToDebug("BeanFactory.refresh()" + resourceSetTypes==null?"":". ResourceSetTypes=" + resourceSetTypes);

		PlatformReadLock lock = s_initLock.readLock();

		lock.lock();
		try
		{
			try
			{
				if ( resourceSetTypes != null && s_beanFactoryToUse instanceof DefaultBeanFactoryImpl )
				{
					return ((DefaultBeanFactoryImpl)s_beanFactoryToUse).refresh(resourceSetTypes);
				}
				else
				{
					return s_beanFactoryToUse.refresh();
				}
			}
			catch(NullPointerException ex)
			{
				if(s_beanFactoryToUse == null)
				{
					throw new BeanFactoryNotOpenedException();
				}
				else
				{
					throw ex;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	private static void logToDebug(String userMessage)
	{
		if (s_logger.isDebugEnabled())
		{
			Thread		thread		= Thread.currentThread();
			String		threadName	= thread.getName();
			long		threadId	= thread.getId();

			String		message		= String.format("(%2$3d) %1$-15s %3$s", threadName, threadId, userMessage);

			s_logger.debug(message);
		}
	}
}



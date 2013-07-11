package org.googlecode.perftrace.javaagent.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

import org.googlecode.perftrace.javassist.CannotCompileException;
import org.googlecode.perftrace.javassist.ClassPath;
import org.googlecode.perftrace.javassist.ClassPool;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.LoaderClassPath;
import org.googlecode.perftrace.javassist.NotFoundException;

 
/**
 * From Tapestry 5
 * Web Server下 classpool下需要添加loader对应的classpath;
 * Used to ensure that {@link org.googlecode.perftrace.javassist.ClassPool#appendClassPath(org.googlecode.perftrace.javassist.ClassPath)} is invoked within a synchronized
 * lock, and also handles tricky class loading issues (caused by the creation of classes, and class loaders, at
 * runtime).
 */
@SuppressWarnings("unchecked")
public class ClassFactoryClassPool extends ClassPool
{
 
    // Kind of duplicating some logic from ClassPool to avoid a deadlock-producing synchronized block.
 
    private static final Method defineClass = findMethod("defineClass", String.class, byte[].class,
                                                         int.class, int.class);
 
    private static final Method defineClassWithProtectionDomain = findMethod("defineClass", String.class, byte[].class,
                                                                             int.class, int.class,
                                                                             ProtectionDomain.class);
	private static Method findMethod(final String methodName, final Class... parameterTypes)
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>()
            {
                public Method run() throws Exception
                {
                    Class cl = Class.forName("java.lang.ClassLoader");
 
                    Method result = cl.getDeclaredMethod(methodName, parameterTypes);
 
                    // Just make it accessible; no particular reason to make it unaccessible again.
 
                    result.setAccessible(true);
 
                    return result;
                }
            });
        }
        catch (PrivilegedActionException ex)
        {
            throw new RuntimeException(String.format("Unable to initialize ClassFactoryClassPool: %s",
                                                     toMessage(ex)), ex);
        }
    }
 
    /**
     * Used to identify which class loaders have already been integrated into the pool.
     */
    private final Set<ClassLoader> allLoaders = CollectionFactory.newSet();
 
    private final Map<ClassLoader, ClassPath> leafLoaders = CollectionFactory.newMap();
 
    public ClassFactoryClassPool(ClassLoader contextClassLoader)
    {
        super(null);
 
        addClassLoaderIfNeeded(contextClassLoader);
    }
 
    /**
     * Returns the nearest super-class of the provided class that can be converted to a {@link CtClass}. This is used to
     * filter out Hibernate-style proxies (created as subclasses of oridnary classes). This will automatically add the
     * class' classLoader to the pool's class path.
     *
     * @param clazz class to import
     * @return clazz, or a super-class of clazz
     */
    public Class importClass(Class clazz)
    {
        addClassLoaderIfNeeded(clazz.getClassLoader());
 
        while (true)
        {
            try
            {
                String name = ClassFabUtils.toJavaClassName(clazz);
 
                get(name);
 
                break;
            }
            catch (NotFoundException ex)
            {
                clazz = clazz.getSuperclass();
            }
        }
 
        return clazz;
    }
 
    /**
     * Convienience method for adding to the ClassPath for a particular class loader.
     * <p/>
     *
     * @param loader the class loader to add (derived from a loaded class, and may be null for some system classes)
     */
    public synchronized void addClassLoaderIfNeeded(ClassLoader loader)
    {
        Set<ClassLoader> leaves = leafLoaders.keySet();
        if (loader == null || leaves.contains(loader) || allLoaders.contains(loader)) return;
 
        // Work out if this loader is a child of a loader we have already.
        ClassLoader existingLeaf = loader;
        while (existingLeaf != null && !leaves.contains(existingLeaf))
        {
            existingLeaf = existingLeaf.getParent();
        }
 
        if (existingLeaf != null)
        {
            // The new loader is a child of an existing leaf.
            // So we remove the old leaf before we add the new loader
            ClassPath priorPath = leafLoaders.get(existingLeaf);
            removeClassPath(priorPath);
            leafLoaders.remove(existingLeaf);
        }
 
        ClassPath path = new LoaderClassPath(loader);
        leafLoaders.put(loader, path);
        insertClassPath(path);
 
        ClassLoader l = loader;
        while (l != null)
        {
            allLoaders.add(l);
            l = l.getParent();
        }
    }
 
    /**
     * Overriden to remove a deadlock producing synchronized block. We expect that the defineClass() methods will have
     * been marked as accessible statically (by this class), so there's no need to set them accessible again.
     */
    @Override
    public Class toClass(CtClass ct, ClassLoader loader, ProtectionDomain domain)
            throws CannotCompileException
    {
        Throwable failure;
 
        try
        {
            byte[] b = ct.toBytecode();
 
            boolean hasDomain = domain != null;
 
            Method method = hasDomain ? defineClassWithProtectionDomain : defineClass;
 
            Object[] args = hasDomain
                            ? new Object[] {ct.getName(), b, 0, b.length, domain}
                            : new Object[] {ct.getName(), b, 0, b.length};
 
            return (Class) method.invoke(loader, args);
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }
 
        throw new CannotCompileException(
                String.format("Failure defining new class %s: %s",
                              ct.getName(),
                              toMessage(failure)), failure);
    }
    
    /**
     * Extracts the message from an exception. If the exception's message is null, returns the exceptions class name.
     *
     * @param exception
     *            to extract message from
     * @return message or class name
     */
    public static String toMessage(Throwable exception)
    {
        String message = exception.getMessage();
 
        if (message != null)
            return message;
 
        return exception.getClass().getName();
    }
}

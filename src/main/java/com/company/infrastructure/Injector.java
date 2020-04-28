package com.company.infrastructure;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// since Dependency injection is best practice, I managed to create my own basic injector
public class Injector{
    private static Logger logger = Logger.getLogger(Injector.class.getName());
    private static Map<String,List<Object>> objects = new HashMap<>();

    private Injector() {
    }

    public static Object getImplementation(Class classType){
        loadObjects(classType);
        return objects.get(classType.getName()).get(0);
    }
    public static List<Object> getImplementations(Class classType){
        loadObjects(classType);
        return Collections.unmodifiableList(objects.get(classType.getName()));
    }

    private static void loadObjects(Class classType){
        if (!objects.containsKey(classType.getName())) {
            ServiceLoader<Object> loader = ServiceLoader.load(classType);
            try {
                Iterator<Object> instances = loader.iterator();
                while (instances.hasNext()) {
                    Object instance = instances.next();
                    if (instance != null) {
                        List<Object> instanceList = new ArrayList<>();
                        instanceList.add(instance);
                        if(objects.containsKey(classType.getName())){
                            instanceList.addAll(objects.get(classType.getName()));
                        }
                        objects.put(classType.getName(),instanceList);
                    }
                }
            } catch (ServiceConfigurationError serviceError) {
                logger.log(Level.SEVERE,serviceError.getMessage());
                throw new BootException("couldn't inject the class object");
            }
        }
    }
}

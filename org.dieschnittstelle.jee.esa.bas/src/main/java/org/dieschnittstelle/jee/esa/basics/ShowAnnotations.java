package org.dieschnittstelle.jee.esa.basics;


import org.dieschnittstelle.jee.esa.basics.annotations.AnnotatedStockItemBuilder;
import org.dieschnittstelle.jee.esa.basics.annotations.DisplayAs;
import org.dieschnittstelle.jee.esa.basics.annotations.StockItemProxyImpl;

import static org.dieschnittstelle.jee.esa.utils.Utils.*;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ShowAnnotations {

    public static void main(String[] args) {
        // we initialise the collection
        StockItemCollection collection = new StockItemCollection(
                "stockitems_annotations.xml", new AnnotatedStockItemBuilder());
        // we load the contents into the collection
        collection.load();

        for (IStockItem consumable : collection.getStockItems()) {

            try {
                showAttributes(((StockItemProxyImpl) consumable).getProxiedObject());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        // we initialise a consumer
        Consumer consumer = new Consumer();
        // ... and let them consume
        consumer.doShopping(collection.getStockItems());
    }

    /*
     * UE BAS2
     */
    private static void showAttributes(Object consumable) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> c = consumable.getClass();

        show("class is: " + c);
        Field[] fields = c.getDeclaredFields();
        System.out.print(c.getSimpleName() + " ");

        for (Field field : fields) {
            for (Method method : c.getMethods()) {
                if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))) {
                    if (method.getName().toLowerCase().contains(field.getName().toLowerCase())) {
                        // MZ: Method found, run it
                        String fieldName = "";
                        if (field.getAnnotation(DisplayAs.class) != null) {
                            fieldName = field.getAnnotation(DisplayAs.class).value();
                        } else {
                            fieldName = field.getName();
                        }
                        System.out.print(fieldName + ": " + method.invoke(consumable) + " ");

                    }
                }
            }
        }
        System.out.println();
    }

}

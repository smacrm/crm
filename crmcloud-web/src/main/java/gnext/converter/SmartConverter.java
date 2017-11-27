package gnext.converter;

import gnext.bean.mente.MenteItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

/**
 *
 * @author hungpham
 * @since May 17, 2017
 */
@FacesConverter(value = "gnext.converter.smart")
public class SmartConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return SelectItemsUtils.findValueByStringConversion(context, component, value, this);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString(); // Can phai override phuong thuoc toString() trong cac object, de tra ve ID cua Databean
    }

}

final class SelectItemsUtils {

    public static Object findValueByStringConversion(FacesContext context, UIComponent component, String value, Converter converter) {
        Iterator<SelectItem> items = createSelectItems(component).iterator();
        return findValueByStringConversion(context, component, items, value, converter);
    }

    private static Object findValueByStringConversion(FacesContext context, UIComponent component, Iterator<SelectItem> items, String value, Converter converter) {
        while (items.hasNext()) {
            SelectItem item = items.next();
            if (item instanceof SelectItemGroup) {
                SelectItem subitems[] = ((SelectItemGroup) item).getSelectItems();
                if (!isEmpty(subitems)) {
                    Object object = findValueByStringConversion(context, component, new ArrayIterator(subitems), value, converter);
                    if (object != null) {
                        return object;
                    }
                }
            } else if (!item.isNoSelectionOption() && value.equals(converter.getAsString(context, component, item.getValue()))) {
                return item.getValue();
            }
        }
        return null;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * This class is based on Mojarra version
     */
    static class ArrayIterator implements Iterator<SelectItem> {

        public ArrayIterator(SelectItem items[]) {
            this.items = items;
        }

        private SelectItem items[];
        private int index = 0;

        public boolean hasNext() {
            return (index < items.length);
        }

        public SelectItem next() {
            try {
                return (items[index++]);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static List<SelectItem> createSelectItems(UIComponent component) {
        List<SelectItem> items = new ArrayList<SelectItem>();
        Iterator<UIComponent> children = component.getChildren().iterator();

        while (children.hasNext()) {
            UIComponent child = children.next();

            if (child instanceof UISelectItem) {
                UISelectItem selectItem = (UISelectItem) child;

                items.add(new SelectItem(selectItem.getItemValue(), selectItem.getItemLabel()));
            } else if (child instanceof UISelectItems) {
                Object selectItems = ((UISelectItems) child).getValue();

                if (selectItems instanceof SelectItem[]) {
                    SelectItem[] itemsArray = (SelectItem[]) selectItems;

                    for (SelectItem item : itemsArray) {
                        items.add(new SelectItem(item.getValue(), item.getLabel()));
                    }

                } else if (selectItems instanceof Collection) {
                    Collection<SelectItem> collection = (Collection<SelectItem>) selectItems;

                    for (SelectItem item : collection) {
                        items.add(new SelectItem(item.getValue(), item.getLabel()));
                    }
                }
            }
        }

        return items;
    }
}

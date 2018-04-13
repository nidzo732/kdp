package pn150121d.kdp.stockmarket.common;

import javax.swing.*;

/**
 * Grafička komponenta za prikaz logova na GUI-ju
 */
public class ScrollableTextList extends JScrollPane
{
    private JList<String> list;

    public ScrollableTextList()
    {
        list = new JList<>(new DefaultListModel<>());
        setViewportView(list);
    }

    public void clear()
    {
        ((DefaultListModel<String>) (list.getModel())).clear();
    }

    public void append(String message)
    {
        ((DefaultListModel<String>) (list.getModel())).addElement(message);
        getVerticalScrollBar().setValue(getVerticalScrollBar().getMaximum());
    }
}

package pn150121d.kdp.stockmarket.common;

import javax.swing.*;

/**
 * Grafička komponenta za prikaz logova na GUI-ju
 */
public class ScrollableTextList extends JScrollPane
{
    private JList<String> list;
    private DefaultListModel<String> model;

    public ScrollableTextList()
    {
        model=new DefaultListModel<>();
        list = new JList<>(model);
        setViewportView(list);
    }

    public synchronized void clear()
    {
        model.clear();
        repaint();
    }

    public synchronized void append(String message)
    {
        model.addElement(message);
        getVerticalScrollBar().setValue(getVerticalScrollBar().getMaximum());
        repaint();
    }
}

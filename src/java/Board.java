
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Piotrek
 */
public class Board {
    Map<Point, FieldValue> board = new HashMap<>();
    private int minX = 0;
    private int maxX = 0;
    private int minY = 0;
    private int maxY = 0;
    
    
    
    public void set(int x, int y, FieldValue value, boolean temp) {
        board.put(new Point(x, y), value);
        if (temp) return;
        if (getMinX() > x) {
            minX = x;
        }
        if (getMaxX() < x) {
            maxX = x;
        }
        if (getMinY() > y) {
            minY = y;
        }
        if (getMaxY() < y) {
            maxY = y;
        }
    }

    public FieldValue get(int x, int y) {
        FieldValue val = board.get(new Point(x, y));
        return null == val ? FieldValue.EMPTY : val;
    }
    public String getHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<style>\n" +
            "table, th, td {\n" +
            "    border: 1px solid black;\n" +
                "border-collapse: collapse;" +
            "}\n" +
            "</style>");
        sb.append("<table>\n");
        for (int y = getMinY(); y <= getMaxY(); y++) {
            sb.append("<tr>");
            for (int x = getMinX(); x <= getMaxX(); x++) {
                sb.append("<td width=13>");
                FieldValue val = get(x,y);
                if (val == FieldValue.CIRCLE) {
                    sb.append("O");
                } else if (val == FieldValue.CROSS) {
                    sb.append("X");
                } else {
                    sb.append("&nbsp;");
                }
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
    
    public int getWidth() {
        return Math.abs(maxX-minX) + 1;
    }
    
    public int getHeight() {
        return Math.abs(maxY-minY) + 1; 
    }
    
    
}

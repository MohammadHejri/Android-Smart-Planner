package com.sharif.project.util;

import java.util.HashMap;
import java.util.Map;

public class PersianNumberConverter {

    private Map<Character, Character> map;

    public PersianNumberConverter() {
        map = new HashMap<>();
        map.put('0', '۰');
        map.put('1', '۱');
        map.put('2', '۲');
        map.put('3', '۳');
        map.put('4', '۴');
        map.put('5', '۵');
        map.put('6', '۶');
        map.put('7', '۷');
        map.put('8', '۸');
        map.put('9', '۹');
    }

    public String convert(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (map.containsKey(c)) {
                result.append(map.get(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

}

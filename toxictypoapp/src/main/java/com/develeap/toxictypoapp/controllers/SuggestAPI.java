package com.develeap.toxictypoapp.controllers;
import com.develeap.toxictypoapp.Throttle;
import com.omrispector.spelling.implementations.SpellChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/suggest")
public class SuggestAPI {
    @Autowired
    private SpellChecker spellChecker;

    @Autowired
    Throttle throttle;

    @RequestMapping(value="{word}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,String> suggest(@PathVariable String word){
        Map<String,String> ret = new HashMap();
        ret.put("q",word);
        String suggestion = spellChecker.correct(word.toLowerCase());
        if (suggestion!=null && suggestion.equalsIgnoreCase(word)) suggestion=null;
        ret.put("suggestion",suggestion);
        throttle.throttle();
        return ret;
    }
}

package com.develeap.toxictypoapp.controllers;
import com.develeap.toxictypoapp.Throttle;
import com.omrispector.spelling.implementations.SpellChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/name")
public class StoreAPI {
    private String data="un-intialized";

    @RequestMapping(value="", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,String> suggest(){
        Map<String,String> ret = new HashMap();
        ret.put("name",data);
        ret.put("action","retrieve");
        return ret;
    }

    @RequestMapping(value="", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,String> suggest(@RequestParam("name") String word){
        Map<String,String> ret = new HashMap();
        ret.put("prevName",data);
        data=word;
        ret.put("name",data);
        ret.put("action","store");
        return ret;
    }
}

package com.develeap.toxictypoapp.configuration;

import com.develeap.toxictypoapp.Throttle;
import com.omrispector.spelling.implementations.FileBasedSpellingDB;
import com.omrispector.spelling.implementations.SpellChecker;
import com.omrispector.spelling.interfaces.SpellingDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TTConfig {
    @Bean
    SpellChecker getSpellChecker() {
        SpellingDB spellingDb = new FileBasedSpellingDB("/somebooks.txt");
        return new SpellChecker(spellingDb);
    }

    @Bean
    Throttle getThrottle() {
        return new Throttle();
    }
}

package com.raji.notes.controller

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WelcomeController(val messageSource: MessageSource) {

    @GetMapping("/welcome")
    fun getWelcomeMessage(): String {
        val locale = LocaleContextHolder.getLocale()
        return messageSource.getMessage("good.morning.message", arrayOf<String>(), locale)
    }


}
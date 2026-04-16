package org.taonity.artistinsightservice.devaccess.service

import org.springframework.mail.SimpleMailMessage

interface AppMailSender {
    fun send(message: SimpleMailMessage)
}
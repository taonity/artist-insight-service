package org.taonity.artistinsightservice.devaccess.service

import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
@Profile("!stub-mail")
class SmtpAppMailSender(
    private val mailSender: JavaMailSender
) : AppMailSender {

    override fun send(message: SimpleMailMessage) {
        mailSender.send(message)
    }
}
package org.taonity.artistinsightservice

import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableStubRunnerServer
@Profile("local")
class LocalStubConfigs 

package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return chat completions"

    request {
        url "/chat/completions"
        method POST()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
            body(
                    id     : "chatcmpl-B9MBs8CjcvOU2jLn4n570S5qMJKcT",
                    object : "chat.completion",
                    created: 1741569952,
                    model  : "gpt-4.1-2025-04-14",
                    choices: [
                            [
                                    index  : 0,
                                    message: [
                                            role       : "assistant",
                                            content    : "[\\\"Ambient\\\",\\\"Dungeon Synth\\\",\\\"Dark Ambient\\\",\\\"Electronic\\\"]",
                                            refusal    : null,
                                            annotations: []
                                    ],
                                    logprobs     : null,
                                    finish_reason: "stop"
                            ]
                    ],
                    usage: [
                            prompt_tokens : 19,
                            completion_tokens: 10,
                            total_tokens : 29,
                            prompt_tokens_details: [
                                    cached_tokens: 0,
                                    audio_tokens : 0
                            ],
                            completion_tokens_details: [
                                    reasoning_tokens            : 0,
                                    audio_tokens                : 0,
                                    accepted_prediction_tokens  : 0,
                                    rejected_prediction_tokens  : 0
                            ]
                    ],
                    service_tier: "default"
            )
        }
    }
}
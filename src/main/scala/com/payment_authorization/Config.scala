package com.payment_authorization

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class Config (
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String
) derives ConfigReader
/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("me.champeau.jmh") version "0.6.4"
}

dependencies {
    api(project(":cosid-core"))
    api("org.springframework.data:spring-data-redis")
    testImplementation("org.junit-pioneer:junit-pioneer")
    jmh("org.openjdk.jmh:jmh-core:${rootProject.ext.get("jmhVersion")}")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:${rootProject.ext.get("jmhVersion")}")
}

jmh {
    jmhVersion.set(rootProject.ext.get("jmhVersion").toString())
    warmupIterations.set(1)
    iterations.set(1)
    resultFormat.set("json")
    benchmarkMode.set(listOf(
        "thrpt"
    ))
//    threads.set(40)
    fork.set(1)
}
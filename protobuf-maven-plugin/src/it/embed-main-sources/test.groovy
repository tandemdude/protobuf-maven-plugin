/*
 * Copyright (C) 2023 - 2024, Ashley Scopes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

Path baseDirectory = basedir.toPath().toAbsolutePath()
Path classesDir = baseDirectory.resolve("target/classes")
def expectedFiles = [
    "helloworld.proto",
    "org/example/message.proto",
    "org/example/channel.proto",
    "org/example/user.proto",
]

assertThat(classesDir).isDirectory()

expectedFiles.forEach {
  assertThat(classesDir.resolve(it))
      .exists()
      .isNotEmptyFile()
}

return true

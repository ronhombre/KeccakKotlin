/*
 * Copyright 2025 Ron Lauren Hombre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *        and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asia.hombre.keccak.api

import asia.hombre.keccak.KeccakParameter

/**
 * An instance defined as a Standard API for `asia.hombre.keccak.*`.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
interface KeccakInstance {
    /**
     * The [asia.hombre.keccak.KeccakParameter] of this instance.
     */
    val parameter: KeccakParameter
}
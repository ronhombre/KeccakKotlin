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

package asia.hombre.keccak.internal


/**
 * A helper class to separate the USABLE part and the HIDDEN part of the state matrix.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
internal class SplitByteArray(var a: ByteArray, var b: ByteArray) {
    val size = a.size + b.size
    val lastIndex = a.size + b.lastIndex
    operator fun get(i: Int): Byte = if(i < a.size) a[i] else b[i - a.size]
    operator fun set(i: Int, value: Byte) = if(i < a.size) a[i] = value else b[i - a.size] = value
}
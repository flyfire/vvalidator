/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.afollestad.vvalidator.form

import android.view.Menu
import android.view.View
import android.widget.AbsSeekBar
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.IdRes
import com.afollestad.vvalidator.ValidationContainer
import com.afollestad.vvalidator.checkAttached
import com.afollestad.vvalidator.field.FieldBuilder
import com.afollestad.vvalidator.field.FormField
import com.afollestad.vvalidator.field.checkable.CheckableField
import com.afollestad.vvalidator.field.input.InputField
import com.afollestad.vvalidator.field.input.InputLayoutField
import com.afollestad.vvalidator.field.seeker.SeekField
import com.afollestad.vvalidator.field.spinner.SpinnerField
import com.afollestad.vvalidator.getViewOrThrow
import com.google.android.material.textfield.TextInputLayout

typealias FormBuilder = Form.() -> Unit

/** @author Aidan Follestad (@afollestad) */
class Form internal constructor(validationContainer: ValidationContainer) {
  var container: ValidationContainer? = validationContainer
  private val fields = mutableListOf<FormField<*, *, *>>()

  /** Adds a field to the form. */
  fun appendField(field: FormField<*, *, *>) {
    fields.add(field)
  }

  /** Retrieves fields that have been added to the form. */
  fun getFields(): List<FormField<*, *, *>> = fields

  /** Adds an input field, which must be a [android.widget.EditText]. */
  fun input(
    view: EditText,
    name: String? = null,
    optional: Boolean = false,
    builder: FieldBuilder<InputField>
  ) {
    val newField = InputField(
        container = container.checkAttached(),
        view = view,
        name = name
    )
    if (optional) {
      newField.isEmptyOr(builder)
    } else {
      builder(newField)
    }
    appendField(newField)
  }

  /** Adds an input field, which must be a [android.widget.EditText]. */
  fun input(
    @IdRes id: Int,
    name: String? = null,
    optional: Boolean = false,
    builder: FieldBuilder<InputField>
  ) = input(
      view = container.getViewOrThrow(id),
      name = name,
      optional = optional,
      builder = builder
  )

  /**
   * Adds an input layout field, which must be a
   * [com.google.android.material.textfield.TextInputLayout]
   */
  fun inputLayout(
    view: TextInputLayout,
    name: String? = null,
    optional: Boolean = false,
    builder: FieldBuilder<InputLayoutField>
  ) {
    val newField = InputLayoutField(
        container = container.checkAttached(),
        view = view,
        name = name
    )
    if (optional) {
      newField.isEmptyOr(builder)
    } else {
      builder(newField)
    }
    appendField(newField)
  }

  /**
   * Adds an input layout field, which must be a
   * [com.google.android.material.textfield.TextInputLayout]
   */
  fun inputLayout(
    @IdRes id: Int,
    name: String? = null,
    optional: Boolean = false,
    builder: FieldBuilder<InputLayoutField>
  ) = inputLayout(
      view = container.getViewOrThrow(id),
      name = name,
      optional = optional,
      builder = builder
  )

  /** Adds a dropdown field, which must be a [android.widget.Spinner]. */
  fun spinner(
    view: Spinner,
    name: String? = null,
    builder: FieldBuilder<SpinnerField>
  ) {
    val newField = SpinnerField(
        container = container.checkAttached(),
        view = view,
        name = name
    )
    builder(newField)
    appendField(newField)
  }

  /** Adds a dropdown field, which must be a [android.widget.Spinner]. */
  fun spinner(
    @IdRes id: Int,
    name: String? = null,
    builder: FieldBuilder<SpinnerField>
  ) = spinner(
      view = container.getViewOrThrow(id),
      name = name,
      builder = builder
  )

  /**
   * Adds a checkable field, like a [android.widget.CheckBox], [android.widget.Switch], or
   * [android.widget.RadioButton].
   */
  fun checkable(
    view: CompoundButton,
    name: String? = null,
    builder: FieldBuilder<CheckableField>
  ) {
    val newField = CheckableField(
        container = container.checkAttached(),
        view = view,
        name = name
    )
    builder(newField)
    appendField(newField)
  }

  /**
   * Adds a checkable field, like a [android.widget.CheckBox], [android.widget.Switch], or
   * [android.widget.RadioButton].
   */
  fun checkable(
    @IdRes id: Int,
    name: String? = null,
    builder: FieldBuilder<CheckableField>
  ) = checkable(
      view = container.getViewOrThrow(id),
      name = name,
      builder = builder
  )

  /** Adds a AbsSeekBar field, like a [android.widget.SeekBar] or [android.widget.RatingBar]. */
  fun seeker(
    view: AbsSeekBar,
    name: String? = null,
    builder: FieldBuilder<SeekField>
  ) {
    val newField = SeekField(
        container = container.checkAttached(),
        view = view,
        name = name
    )
    builder(newField)
    appendField(newField)
  }

  /** Adds a AbsSeekBar field, like a [android.widget.SeekBar] or [android.widget.RatingBar]. */
  fun seeker(
    @IdRes id: Int,
    name: String? = null,
    builder: FieldBuilder<SeekField>
  ) = seeker(
      view = container.getViewOrThrow(id),
      name = name,
      builder = builder
  )

  /** Validates all fields in the form. */
  fun validate(): FormResult {
    val finalResult = FormResult()
    for (field in fields) {
      val fieldResult = field.validate()
      finalResult += fieldResult
    }
    return finalResult
  }

  /**
   * Attaches the form to a view. When the view is clicked, validation is performed. If
   * validation passes, the given callback is invoked.
   */
  fun submitWith(
    view: View,
    onSubmit: (FormResult) -> Unit
  ) = view.setOnClickListener {
    val result = validate()
    if (result.success()) {
      onSubmit(result)
    }
  }

  /**
   * Attaches the form to a view. When the view is clicked, validation is performed. If
   * validation passes, the given callback is invoked.
   */
  fun submitWith(
    @IdRes id: Int,
    onSubmit: (FormResult) -> Unit
  ) {
    val currentContainer = container.checkAttached()
    val view = currentContainer.findViewById<View>(id) ?: throw IllegalArgumentException(
        "Unable to find view ${currentContainer.getFieldName(id)} in your container."
    )
    submitWith(view, onSubmit)
  }

  /**
   * Attaches the form to a menu item. When the item is clicked, validation is performed. If
   * validation passes, the given callback is invoked.
   */
  fun submitWith(
    menu: Menu,
    @IdRes itemId: Int,
    onSubmit: (FormResult) -> Unit
  ) {
    val currentContainer = container.checkAttached()
    val item = menu.findItem(itemId) ?: throw IllegalArgumentException(
        "Didn't find item ${currentContainer.getFieldName(itemId)} in the given Menu."
    )
    item.setOnMenuItemClickListener {
      val result = validate()
      if (result.success()) {
        onSubmit(result)
      }
      true
    }
  }

  /** Destroys the form by removing all fields and freeing up references. */
  internal fun destroy() {
    fields.clear()
    container = null
  }
}

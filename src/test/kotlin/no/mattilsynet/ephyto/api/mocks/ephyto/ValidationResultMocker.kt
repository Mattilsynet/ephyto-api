package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.ValidationArea
import _int.ippc.ephyto.ValidationLevel
import _int.ippc.ephyto.ValidationResult

object ValidationResultMocker {

    fun createValidationResultMock(
        level: ValidationLevel = ValidationLevel.INFO,
    ): ValidationResult =
        ValidationResult().also {
            it.area = ValidationArea.MAPPING
            it.field = "Field"
            it.level = level
            it.msg = "Validation message"
        }

}

package br.com.zup.edu.config.validation

import br.com.zup.edu.chaves.dto.ChaveRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint

@MustBeDocumented
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Constraint(validatedBy = [ChaveValidator::class])
annotation class ChaveValida(
    val message: String = "Chave inválida"
)

@Singleton
class ChaveValidator : ConstraintValidator<ChaveValida, ChaveRequest> {
    override fun isValid(
        value: ChaveRequest,
        annotationMetadata: AnnotationValue<ChaveValida>,
        context: ConstraintValidatorContext
    ): Boolean {
        return value.tipo.valida(value.chave)
    }
}
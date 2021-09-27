package br.com.zup.edu.chaves.dto

enum class TipoChaveLocal {
    CPF {
        override fun valida(chave: String?): Boolean {
            return if (chave.isNullOrBlank())
                false
            else
                chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    TELEFONE {
        override fun valida(chave: String?): Boolean {
            return if (chave.isNullOrBlank())
                false
            else
                chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            return if (chave.isNullOrBlank())
                false
            else
                chave.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+\$".toRegex())
        }
    },
    RANDOM {
        override fun valida(chave: String?): Boolean = chave.isNullOrBlank()
    };

    abstract fun valida(chave: String?): Boolean
}
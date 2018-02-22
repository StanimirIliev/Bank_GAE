import React from 'react'
import PropTypes from 'prop-types'

const Money = ({currency, amount, digits}) => {
    const formattedAmount = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: currency
      }).format(amount.toFixed(digits))
    return (
        <span>{formattedAmount}</span>
    )
}

Money.propTypes = {
    currency: PropTypes.string.isRequired,
    amount: PropTypes.number.isRequired,
    digits: PropTypes.number.isRequired
}

export default Money
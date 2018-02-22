import React from 'react'
import './Account.css'
import Money from '../Money'

const Account = ({ title, balance, currency }) => (
  <div className="container__account">
    <div className="container__account__title">
      <b>Name:</b> {title}
    </div>
    <div className="container__account__balance">
      <b>Balance:</b> <Money amount={balance} currency={currency} digits={2} />
    </div>
  </div>
)

export default Account

import React from 'react'
import { Link, Route } from 'react-router-dom'
import AccountsMenu from '../AccountsMenu'
import ShowAccounts from '../../SubMenus/ShowAccounts'
import AccountDetails from '../../SubMenus/AccountDetails'
import Deposit from '../../Operations/Deposit'
import Withdraw from '../../Operations/Withdraw'
import NewAccount from '../../SubMenus/NewAccount'
import RemoveAccountsMenu from '../RemoveAccountMenu'
import RemoveAccount from '../../SubMenus/RemoveAccount'
import Transactions from '../Transactions'
import './MainMenu.css'

const Menu = (match) => (
    <div className="main-menu" >
        <Link className="linkButton button--primary" to="/transactions/1">Transactions</Link>
        <Link className="linkButton button--primary" to="/main">Accounts</Link>
        <a className="linkButton button--primary" href="/logout">Log out</a>
        <Route exact path="/main" component={AccountsMenu} />
        <Route exact path="/accounts" component={ShowAccounts} />
        <Route exact path="/accounts/:id/" component={AccountDetails} />
        <Route exact path="/accounts/:id/deposit" component={Deposit} />
        <Route exact path="/accounts/:id/withdraw" component={Withdraw} />
        <Route exact path="/new" component={NewAccount} />
        <Route exact path="/delete" component={RemoveAccountsMenu} />
        <Route exact path="/accounts/:id/delete" component={RemoveAccount} />
        <Route path="/transactions" component={Transactions} />
    </div>
)

export default Menu

import React from 'react'
import { Link } from 'react-router-dom'
import BackButton from '../../Common/BackButton'
import './AccountsMenu.css'

const AccountsMenu = () => (
    <div className="container__accounts" >
        <div className="container__options" >
            <Link to="/accounts" className="container__options__item">
                <img className="icon" src={require("../../../icons/list.png")} />
                <div className="item__text">
                    Show my accounts
                </div>
            </Link>
            <Link to="/new" className="container__options__item">
                <img className="icon" src={require("../../../icons/add.png")} />
                <div className="item__text">
                    Add new account
                </div>
            </Link>
            <Link to="/delete" className="container__options__item">
                <img className="icon" src={require("../../../icons/remove.png")} />
                <div className="item__text">
                    Delete account
                </div>
            </Link>
        </div>
        <BackButton to="/" name="Close"/>
    </div>
)

export default AccountsMenu

import React, { Component } from 'react'
import { Link } from 'react-router-dom'
import './Navigation.css'

export default class Navigation extends Component {
    constructor(props) {
        super(props)
        this.state = {
            pages: this.props.pages,
            currentPage: 1
        }
    }
    render() {
        const navBar = []
        const { pages, currentPage } = this.state
        const maxBoxes = 7
        const middle = parseInt(maxBoxes / 2) + 1
        let boxesLeft = maxBoxes

        for (let i = 1; i <= pages; i++) {
            if (boxesLeft > 0 && (currentPage <= middle || i > currentPage - middle || i > pages - maxBoxes)) {
                if (i === currentPage) {
                    navBar.push(
                        <li key={i} className="transactions__navigation__item--current">{i}</li>)
                }
                else {
                    navBar.push(
                        <li key={i} className="transactions__navigation__item">
                            <Link className="transactions__navigation__link" onClick={() => { this.setState({ currentPage: i }) }} to={`/transactions/${i}`} >{i}</Link>
                        </li>)
                }
                boxesLeft--
            }
        }

        return (
            <div className="transactions__navigation__container">
                <ul className="transactions__navigation">
                    {navBar}
                </ul>
            </div>
        )
    }
}
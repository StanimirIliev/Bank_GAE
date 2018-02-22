import React from 'react'
import {Link} from 'react-router-dom'
import PropTypes from 'prop-types'
import './BackButton.css'

const BackButton = ({to, name}) => {
    return (<Link to={to} className="linkButton button--close">{name}</Link>)
}

BackButton.propTypes = {
    to: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired
}

export default BackButton
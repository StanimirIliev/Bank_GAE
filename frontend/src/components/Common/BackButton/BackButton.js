import React from 'react'
import {Link} from 'react-router-dom'
import PropTypes from 'prop-types'
import './BackButton.css'

const BackButton = ({to, name}) => {
    return (<Link to={to} className="linkButton button--back">Back</Link>)
}

BackButton.propTypes = {
    to: PropTypes.string.isRequired
}

export default BackButton
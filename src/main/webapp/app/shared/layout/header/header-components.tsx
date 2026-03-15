import React from 'react';
import { Translate } from 'react-jhipster';

import { NavItem, NavLink, NavbarBrand } from 'reactstrap';
import { NavLink as Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

// @ts-expect-error: TS doesn't recognize PNG imports natively
import myLogo from '../../../../content/images/DeFi_Logo_T.png';

export const BrandIcon = props => (
  <div
    {...props}
    className="brand-icon"
    style={{
      display: 'flex',
      alignItems: 'center',
      height: '100%',
      paddingLeft: '10px',
    }}
  >
    <img
      src="content/images/DeFi_Logo_T.png"
      alt="Logo"
      style={{
        height: '80px',
        width: 'auto',
        objectFit: 'contain',
      }}
    />
  </div>
);

export const Brand = () => (
  <NavbarBrand tag={Link} to="/" className="brand-logo" style={{ height: '110px', display: 'flex', alignItems: 'center' }}>
    <BrandIcon />
  </NavbarBrand>
);

export const Home = () => (
  <NavItem>
    <NavLink tag={Link} to="/" className="d-flex align-items-center">
      <FontAwesomeIcon icon="home" />
      <span>
        <Translate contentKey="global.menu.home">Home</Translate>
      </span>
    </NavLink>
  </NavItem>
);

export const GameLink = () => (
  <NavItem>
    <NavLink tag={Link} to="/games" className="d-flex align-items-center">
      Game
    </NavLink>
  </NavItem>
);

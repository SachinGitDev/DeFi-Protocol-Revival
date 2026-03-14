import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Row, Col, Input, Button, Card, CardBody, CardTitle, CardText, Form, FormGroup } from 'reactstrap';

export const Home = () => {
  const [githubUrl, setGithubUrl] = useState('');
  const navigate = useNavigate();

  const handleAnalyse = event => {
    event.preventDefault();
    if (githubUrl.trim() === '') {
      alert('Please enter a valid GitHub URL first!');
      return;
    }
    navigate(`/details?repo=${encodeURIComponent(githubUrl)}`);
  };

  return (
    /* This wrapper div adds the greyish-white background to the whole page */
    <div style={{ backgroundColor: '#ffffff', minHeight: '100vh', paddingTop: '40px', paddingBottom: '40px' }}>
      <Container className="text-center">
        <Row className="justify-content-center">
          <Col md="10" className="d-flex justify-content-center">
            <div
              style={{
                backgroundColor: '#1a1d21',
                padding: '20px 40px', // Reduced top/bottom padding to make it "smaller"
                borderRadius: '20px',
                boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
                width: 'fit-content', // This makes the pod only as wide as the logo
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                marginBottom: '30px',
                marginTop: '20px',
              }}
            >
              <img
                src="content/images/DeFi_Logo_T.png"
                alt="Revival Logo"
                style={{
                  width: 'auto',
                  maxWidth: '450px', // Slightly smaller scale for a tighter fit
                  height: 'auto',
                }}
              />
            </div>
          </Col>
        </Row>

        <Row className="justify-content-center mb-5">
          <Col md="8">
            <Card className="p-4 shadow-sm" style={{ borderRadius: '15px' }}>
              <h4 className="mb-4">Paste the github URL</h4>
              <Form onSubmit={handleAnalyse}>
                <FormGroup className="d-flex justify-content-center">
                  <Input
                    type="url"
                    name="githubLink"
                    id="githubLink"
                    placeholder="Enter the link here..."
                    value={githubUrl}
                    onChange={e => setGithubUrl(e.target.value)}
                    style={{ width: '60%', marginRight: '10px', borderRadius: '20px' }}
                  />
                  <Button color="primary" type="submit" style={{ borderRadius: '20px', padding: '0 30px' }}>
                    Analyse
                  </Button>
                </FormGroup>
              </Form>
            </Card>
          </Col>
        </Row>

        <Row className="justify-content-center">
          <Col md="8">
            <Card className="text-left bg-light shadow-sm" style={{ borderRadius: '15px' }}>
              <CardBody>
                <div className="mb-4 text-start">
                  <CardTitle tag="h4" className="font-weight-bold">
                    What Is This Project?
                  </CardTitle>
                  <CardText>
                    This program looks through repositories searching for decentralised finance contracts. It checks for any abandoned
                    protocols with any vulnerabilities. It will then generate code to resurrect the code. It will also use an LLM to detect
                    misinformation in the comments of the code and the README file.
                  </CardText>
                </div>

                <div className="mb-4 text-start">
                  <CardTitle tag="h4" className="font-weight-bold">
                    Why Does It Matter?
                  </CardTitle>
                  <CardText>
                    The DeFi space moves at lightning speed, often leaving behind innovative protocols that still hold significant user
                    value. However, these abandoned contracts become zombie code, vulnerable targets for exploits that can drain funds and
                    damage market trust. Revival bridges this gap by identifying these high-potential protocols and using advanced AI to
                    patch critical vulnerabilities and correct misleading documentation. By resurrecting these contracts, we transform
                    dangerous liabilities into secure, functional assets, ensuring the DeFi ecosystem remains resilient, transparent, and
                    safe for all users.
                  </CardText>
                </div>
              </CardBody>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default Home;

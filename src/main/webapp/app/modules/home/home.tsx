import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Row, Col, Input, Button, Card, CardBody, CardTitle, CardText, Form, FormGroup, Spinner } from 'reactstrap';
import axios from 'axios'; // <-- 1. Import Axios!

export const Home = () => {
  const [githubUrl, setGithubUrl] = useState('');
  const [isScanning, setIsScanning] = useState(false); // <-- 2. Add a loading state
  const navigate = useNavigate();

  const handleAnalyse = async event => {
    event.preventDefault();
    if (githubUrl.trim() === '') {
      alert('Please enter a valid GitHub URL first!');
      return;
    }

    // 3. Start the loading spinner
    setIsScanning(true);

    try {
      // 4. Send the URL to Sachin's Backend!
      const response = await axios.post(
        '/api/scan-repository',
        {
          githubUrl,
        },
        { timeout: 600000 },
      );

      // The backend returns a list of processed contracts. We grab the first one.
      const scannedContract = response.data[0];

      if (scannedContract && scannedContract.id) {
        // 5. SUCCESS! Navigate to the details page, passing the real Contract ID!
        setIsScanning(false);
        navigate(`/details?repo=${encodeURIComponent(githubUrl)}&contractId=${scannedContract.id}`);
      } else {
        alert('Scan completed, but no solidity files were found or processed.');
        setIsScanning(false);
      }
    } catch (error) {
      console.error('Backend scan failed:', error);
      alert('Error connecting to the AI Engine. Check the console.');
      setIsScanning(false);
    }
  };

  return (
    <div style={{ backgroundColor: 'transparent', minHeight: '100vh', paddingTop: '40px', paddingBottom: '40px' }}>
      <Container className="text-center">
        {/* LOGO SECTION (I left Rahul's styles exactly as they were) */}
        <Row className="justify-content-center">
          <Col md="10" className="d-flex justify-content-center">
            <div
              style={{
                backgroundColor: '#1a1d21',
                padding: '20px 40px',
                borderRadius: '20px',
                boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
                width: 'fit-content',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                marginBottom: '30px',
                marginTop: '20px',
              }}
            >
              <img src="content/images/DeFi_Logo_T.png" alt="Revival Logo" style={{ width: 'auto', maxWidth: '450px', height: 'auto' }} />
            </div>
          </Col>
        </Row>

        {/* INPUT SECTION */}
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
                    disabled={isScanning} // <-- Disable input while scanning
                  />
                  <Button color="primary" type="submit" style={{ borderRadius: '20px', padding: '0 30px' }} disabled={isScanning}>
                    {isScanning ? <Spinner size="sm" /> : 'Analyse'} {/* Show spinner if scanning */}
                  </Button>
                </FormGroup>
              </Form>
            </Card>
          </Col>
        </Row>

        {/* INFO SECTION (Left exactly as Rahul wrote it) */}
        <Row className="justify-content-center">
          <Col md="8">
            <Card className="text-left shadow-sm" style={{ borderRadius: '15px', background: 'rgba(255,255,255,0.75)' }}>
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

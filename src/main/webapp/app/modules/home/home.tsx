import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Row, Col, Input, Button, Card, CardBody, CardTitle, CardText, Form, FormGroup } from 'reactstrap';

export const Home = () => {
  const [githubUrl, setGithubUrl] = useState('');

  const navigate = useNavigate();

  const handleAnalyse = event => {
    event.preventDefault(); // prevents the page from refreshing

    if (githubUrl.trim() === '') {
      alert('Please enter a valid GitHub URL first!');
      return;
    }

    // navigate to the results page and pass the URL in the query string
    navigate(`/details?repo=${encodeURIComponent(githubUrl)}`);
  };

  return (
    <Container className="text-center mt-5">
      {/* this will represent the title of the project*/}
      <Row>
        <Col>
          <h1 className="display-3 mb-5" style={{ fontWeight: 'bold' }}>
            Revival
          </h1>
        </Col>
      </Row>

      {/* this will act as the initial search box which they will enter the github link into*/}
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

      {/* this is the bulk of the information about the project*/}
      <Row className="justify-content-center">
        <Col md="8">
          <Card className="text-left bg-light shadow-sm" style={{ borderRadius: '15px' }}>
            <CardBody>
              {/* this is the inital card information*/}
              <div className="mb-4">
                <CardTitle tag="h4" className="font-weight-bold">
                  What Is This Project?
                </CardTitle>
                <CardText>
                  This program looks through repositories searching for decentralised finance contracts. It checks for any abandoned
                  protocols with any vulnerabilities. It will then generate code to resurrect the code. It will also use an LLM to detect
                  misinformation in the comments of the code and the README file.
                </CardText>
              </div>

              {/* this is the second card set of information*/}
              <div className="mb-4">
                <CardTitle tag="h4" className="font-weight-bold">
                  Why Does It Matter?
                </CardTitle>
                <CardText>
                  This program looks through repositories searching for decentralised finance contracts. It checks for any abandoned
                  protocols with any vulnerabilities. It will then generate code to resurrect the code. It will also use an LLM to detect
                  misinformation.
                </CardText>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Home;

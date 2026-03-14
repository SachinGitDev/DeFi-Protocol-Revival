import React from 'react';
import { Container, Row, Col, Card, CardBody, Progress, Button } from 'reactstrap';
import { useNavigate, useLocation } from 'react-router-dom';

export const Details = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const repoUrl = new URLSearchParams(location.search).get('repo') || 'DeFi-Contract';

  const stats = {
    name: 'LendingProtocol.sol',
    analysis: [
      {
        issue: 'Reentrancy Protection',
        oldScore: 2,
        newScore: 9,
      },
      {
        issue: 'Math Safety (Overflows)',
        oldScore: 4,
        newScore: 10,
      },
      {
        issue: 'Access Control Logic',
        oldScore: 3,
        newScore: 8,
      },
    ],
  };

  return (
    <Container className="mt-5 pt-5 text-center">
      <h2 className="mb-2">
        Security Analysis: <span className="text-primary">{stats.name}</span>
      </h2>
      <p className="text-muted mb-5">{repoUrl}</p>

      <Row className="justify-content-center">
        <Col md="10">
          <Card className="shadow-sm border-0" style={{ borderRadius: '20px' }}>
            <CardBody className="p-5">
              <h4 className="mb-5">Code Quality Comparison</h4>

              {stats.analysis.map((item, index) => (
                <div key={index} className="mb-5 p-3 border-bottom">
                  <h5 className="text-start mb-3" style={{ fontWeight: 'bold' }}>
                    {item.issue}
                  </h5>

                  <Row className="align-items-center mb-3">
                    <Col xs="3" className="text-end text-muted small">
                      OLD QUALITY
                    </Col>
                    <Col xs="7">
                      <Progress value={item.oldScore * 10} color="danger" style={{ height: '10px' }} />
                    </Col>
                    <Col xs="2" className="text-start font-weight-bold text-danger">
                      {item.oldScore}/10
                    </Col>
                  </Row>

                  <Row className="align-items-center">
                    <Col xs="3" className="text-end text-muted small">
                      REVIVED QUALITY
                    </Col>
                    <Col xs="7">
                      <Progress value={item.newScore * 10} color="success" style={{ height: '10px' }} />
                    </Col>
                    <Col xs="2" className="text-start font-weight-bold text-success">
                      {item.newScore}/10
                    </Col>
                  </Row>
                </div>
              ))}

              <div className="mt-4">
                <Button
                  color="success"
                  size="lg"
                  style={{ borderRadius: '30px', padding: '15px 60px', fontWeight: 'bold', fontSize: '1.2rem' }}
                  onClick={() => navigate(`/resurrect?repo=${encodeURIComponent(repoUrl)}`)}
                >
                  Generate Revived Code
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Details;
